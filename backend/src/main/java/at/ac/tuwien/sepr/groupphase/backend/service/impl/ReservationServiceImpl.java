package at.ac.tuwien.sepr.groupphase.backend.service.impl;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EquipmentMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ReservationMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.ReservationRelation;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationServiceImpl implements at.ac.tuwien.sepr.groupphase.backend.service.ReservationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ReservationMapper reservationMapper;
    private final ReservationValidator validator;
    private final ReservationRepository reservationRepository;
    private final EquipmentRepository equipmentRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final EquipmentMapper equipmentMapper;

    @Autowired
    public ReservationServiceImpl(ReservationMapper reservationMapper,
                                  ReservationRepository reservationRepository,
                                  EquipmentRepository equipmentRepository,
                                  CustomerProfileRepository customerProfileRepository,
                                  ReservationValidator validator, EquipmentMapper equipmentMapper) {
        this.reservationMapper = reservationMapper;
        this.reservationRepository = reservationRepository;
        this.equipmentRepository = equipmentRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.validator = validator;
        this.equipmentMapper = equipmentMapper;
    }


    @Override
    @Transactional
    public ReservationDetailDto reservationById(Long id) {
        LOGGER.trace("Get reservation by id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        if (id < 0) {
            throw new IllegalArgumentException("id is negative");
        }

        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Reservation with ID " + id + " was not found."));

        return reservationMapper.entityToDetailDto(reservation);
    }

    @Override
    @Transactional
    public ReservationDetailDto createReservation(ReservationCreationDto dto) {
        LOGGER.trace("create reservation");
        CustomerProfile profile = customerProfileRepository.findById(dto.getCustomerProfileId())
            .orElseThrow(() -> new NotFoundException("Customer profile with ID " + dto.getCustomerProfileId() + " not found."));

        Reservation reservation = new Reservation(
            profile,
            dto.getPickUpTime(),
            dto.getStartDate(),
            dto.getEndDate()
        );

        for (Long equipmentId : dto.getEquipmentIds()) {
            Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new NotFoundException("Equipment with ID " + equipmentId + " not found."));

            LocalDate pickUpDate = dto.getStartDate();
            LocalDate dropOffDate = dto.getEndDate();

            List<String> validationErrors = new ArrayList<>();
            validator.isEquipmentAvailable(equipment, pickUpDate, dropOffDate, validationErrors);
            if (!validationErrors.isEmpty()) {
                throw new ValidationException("Validation failed for reservation creation", validationErrors);
            }
            reservation.addItem(equipment);

            equipment.addTimePeriod(pickUpDate, dropOffDate, PeriodType.RENTED, reservation);
        }

        Reservation savedReservation = reservationRepository.save(reservation);
        //bestätigungs-email senden
        return reservationMapper.entityToDetailDto(savedReservation);
    }

    @Override
    @Transactional
    public void deleteReservation(Long id) {
        LOGGER.trace("Deleting reservation with id {}", id);

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        if (id < 0) {
            throw new IllegalArgumentException("id is negative");
        }

        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() ->
                new NotFoundException("Reservation with ID " + id + " was not found.")
            );

        LocalDate start = reservation.getStartDate();
        LocalDate end = reservation.getEndDate();

        deleteTimePeriodsForEquipment(reservation.getItems().stream().map(ReservationRelation::getEquipment).toList(), reservation);

        //bestätigungs-mail senden

        reservationRepository.delete(reservation);

    }

    @Override
    @Transactional
    public ReservationDetailDto updateReservation(ReservationUpdateDto dto) {
        Long id = dto.getId();
        LOGGER.trace("update reservation {}", id);

        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Reservation not found"));

        validator.validateUpdateDto(dto);

        boolean datesChanged = (dto.getStartDate() != null && !dto.getStartDate().equals(reservation.getStartDate()))
            || (dto.getEndDate() != null && !dto.getEndDate().equals(reservation.getEndDate()));
        boolean equipmentChanged = dto.getEquipmentIds() != null;

        if (datesChanged || equipmentChanged) {
            List<Equipment> currentEquipments = reservation.getItems().stream()
                .map(ReservationRelation::getEquipment).toList();
            deleteTimePeriodsForEquipment(currentEquipments, reservation);
        }

        if (dto.getPickUpTime() != null) {
            reservation.setPickUpTime(dto.getPickUpTime());
        }
        if (dto.getStartDate() != null) {
            reservation.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            reservation.setEndDate(dto.getEndDate());
        }

        if (dto.getCustomerProfileId() != null) {
            CustomerProfile profile = customerProfileRepository.findById(dto.getCustomerProfileId())
                .orElseThrow(() -> new NotFoundException("Customer profile with ID " + dto.getCustomerProfileId() + " not found."));
            reservation.setCustomerProfile(profile);
        }

        List<Equipment> finalEquipmentsToReserve;
        if (equipmentChanged) {
            reservation.getItems().clear();
            finalEquipmentsToReserve = equipmentRepository.findAllById(dto.getEquipmentIds());
            for (Equipment eq : finalEquipmentsToReserve) {
                reservation.addItem(eq);
            }
        } else {
            finalEquipmentsToReserve = reservation.getItems().stream()
                .map(ReservationRelation::getEquipment).toList();
        }

        if (datesChanged || equipmentChanged) {
            LocalDate newStart = reservation.getStartDate();
            LocalDate newEnd = reservation.getEndDate();

            equipmentRepository.flush();

            for (Equipment equipment : finalEquipmentsToReserve) {
                List<String> validationErrors = new ArrayList<>();
                validator.isEquipmentAvailable(equipment, newStart, newEnd, validationErrors);
                if (!validationErrors.isEmpty()) {
                    throw new ValidationException("Validation failed for reservation creation", validationErrors);
                }
                equipment.addTimePeriod(newStart, newEnd, PeriodType.RENTED, reservation);
            }
        }

        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.entityToDetailDto(saved);
    }

    @Transactional
    @Override
    public List<ReservationDetailDto> searchReservations(ReservationSearchDto searchDto) {
        if (searchDto == null) {
            searchDto = new ReservationSearchDto();
        }

        final ReservationSearchDto finalSearchDto = searchDto;

        Specification<Reservation> spec = (root, query, cb) -> cb.conjunction();

        if (finalSearchDto.getCustomerProfileId() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("customerProfile").get("id"), finalSearchDto.getCustomerProfileId())
            );
        }

        if (finalSearchDto.getAccountId() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("customerProfile").get("customer").get("id"), finalSearchDto.getAccountId())
            );
        }

        if (finalSearchDto.getStartDate() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("startDate"), finalSearchDto.getStartDate())
            );
        }

        if (finalSearchDto.getEndDate() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("endDate"), finalSearchDto.getEndDate())
            );
        }

        if (finalSearchDto.getPickUpTime() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("pickUpTime"), finalSearchDto.getPickUpTime())
            );
        }

        if (finalSearchDto.getSearchRangeStart() != null) {
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("endDate"), finalSearchDto.getSearchRangeStart())
            );
        }

        if (finalSearchDto.getSearchRangeEnd() != null) {
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("startDate"), finalSearchDto.getSearchRangeEnd())
            );
        }

        if (finalSearchDto.getEquipmentIds() != null && !finalSearchDto.getEquipmentIds().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);

                return root.join("items").join("equipment").get("id")
                    .in(finalSearchDto.getEquipmentIds());
            });
        }

        List<Reservation> foundReservations = reservationRepository.findAll(spec);

        return foundReservations.stream()
            .map(reservationMapper::entityToDetailDto)
            .toList();
    }

    @Override
    @Transactional
    public ReservationDetailDto addEquipmentToReservation(ReservationAddDeleteEquipmentDto dto) {

        validator.validateReservationAddEquip(dto);

        Reservation reservation = reservationRepository.findById(dto.getId()).orElseThrow(() ->
            new NotFoundException("Reservation with ID " + dto.getId() + " not found.")
        );

        List<Equipment> equipmentList =
            equipmentRepository.findAllById(dto.getEquipmentIds());

        LocalDate start = reservation.getStartDate();
        LocalDate end = reservation.getEndDate();

        for (Equipment equipment : equipmentList) {
            reservation.addItem(equipment);
            equipment.addTimePeriod(start, end, PeriodType.RENTED, reservation);
        }

        return reservationMapper.entityToDetailDto(reservation);
    }

    @Override
    @Transactional
    public ReservationDetailDto removeEquipmentFromReservation(ReservationAddDeleteEquipmentDto dto) {
        validator.validateReservationRemoveEquipment(dto);

        Reservation reservation = reservationRepository.findById(dto.getId()).orElseThrow(() ->
            new NotFoundException("Reservation with ID " + dto.getId() + " not found.")
        );

        List<Equipment> equipmentList = equipmentRepository.findAllById(dto.getEquipmentIds());

        LocalDate start = reservation.getStartDate();
        LocalDate end = reservation.getEndDate();

        deleteTimePeriodsForEquipment(equipmentList, reservation);

        reservation.getItems().removeIf(relation -> dto.getEquipmentIds().contains(relation.getEquipment().getId()));

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.entityToDetailDto(savedReservation);
    }

    private void deleteTimePeriodsForEquipment(List<Equipment> equipmentList, Reservation reservation) {
        for (Equipment equipment : equipmentList) {
            equipment.getTimePeriodsList().removeIf(tp ->
                tp.getPeriodType() == PeriodType.RENTED
                    && tp.getReservation() != null
                    && tp.getReservation().getId().equals(reservation.getId())
            );
        }
    }


}
