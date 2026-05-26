package at.ac.tuwien.sepr.groupphase.backend.service.impl;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EquipmentMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ReservationMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.ReservationRelation;
import at.ac.tuwien.sepr.groupphase.backend.entity.TimePeriods;
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
            dto.getPickUpDate(),
            dto.getRentDurationDays()
        );

        for (Long equipmentId : dto.getEquipmentIds()) {
            Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new NotFoundException("Equipment with ID " + equipmentId + " not found."));

            LocalDate pickUpDate = dto.getPickUpDate();
            LocalDate dropOffDate = pickUpDate.plusDays(dto.getRentDurationDays() - 1);

            //muss noch validiert werden
            reservation.addItem(equipment);

            equipment.addTimePeriod(pickUpDate, dropOffDate, PeriodType.RENTED);
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

        LocalDate start = reservation.getPickUpDate();
        LocalDate end = reservation.getReturnDate();

        deleteTimePeriodsForEquipment(reservation.getItems().stream().map(ReservationRelation::getEquipment).toList(), start, end);

        //bestätigungs-mail senden

        reservationRepository.delete(reservation);

    }

    @Override
    @Transactional
    public ReservationDetailDto updateReservation(ReservationUpdateDto dto) {
        Long id = dto.getId();
        LOGGER.trace("update reservation {}", id);

        validator.validateUpdateDto(dto);


        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() ->
                new NotFoundException("Reservation not found")
            );

        final LocalDate oldStart = reservation.getPickUpDate();
        final LocalDate oldEnd = reservation.getReturnDate();

        if (dto.getPickUpTime() != null) {
            reservation.setPickUpTime(dto.getPickUpTime());
        }

        if (dto.getPickUpDate() != null) {
            reservation.setPickUpDate(dto.getPickUpDate());
        }

        if (dto.getRentDurationDays() != null) {
            reservation.setRentDurationDays(dto.getRentDurationDays());
        }

        LocalDate newStart = reservation.getPickUpDate();
        LocalDate newEnd = newStart.plusDays(reservation.getRentDurationDays() - 1);

        //update Customer

        if (dto.getCustomerProfileId() != null) {

            CustomerProfile profile =
                customerProfileRepository.findById(dto.getCustomerProfileId())
                    .orElseThrow(() ->
                        new NotFoundException(
                            "Customer profile with ID "
                                + dto.getCustomerProfileId()
                                + " not found."
                        )
                    );

            reservation.setCustomerProfile(profile);
        }


        if (dto.getEquipmentIds() != null) {

            deleteTimePeriodsForEquipment(reservation.getItems().stream().map(ReservationRelation::getEquipment).toList(), oldStart, oldEnd);


            reservation.getItems().clear();

            List<Equipment> equipmentList =
                equipmentRepository.findAllById(dto.getEquipmentIds());

            for (Equipment equipment : equipmentList) {
                reservation.addItem(equipment);
                equipment.addTimePeriod(newStart, newEnd, PeriodType.RENTED);
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

        if (finalSearchDto.getPickUpDate() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("pickUpDate"), finalSearchDto.getPickUpDate())
            );
        }

        if (finalSearchDto.getPickUpTime() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("pickUpTime"), finalSearchDto.getPickUpTime())
            );
        }

        if (finalSearchDto.getTimePeriod() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("timePeriod"), finalSearchDto.getTimePeriod())
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

        LocalDate start = reservation.getPickUpDate();
        LocalDate end = start.plusDays(reservation.getRentDurationDays());

        for (Equipment equipment : equipmentList) {
            reservation.addItem(equipment);
            equipment.addTimePeriod(start, end, PeriodType.RENTED);
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

        LocalDate start = reservation.getPickUpDate();
        LocalDate end = start.plusDays(reservation.getRentDurationDays());

        deleteTimePeriodsForEquipment(equipmentList, start, end);

        reservation.getItems().removeIf(relation -> dto.getEquipmentIds().contains(relation.getEquipment().getId()));

        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.entityToDetailDto(savedReservation);
    }

    private void deleteTimePeriodsForEquipment(List<Equipment> equipmentList, LocalDate start, LocalDate end) {
        for (Equipment equipment : equipmentList) {
            List<TimePeriods> periods = new ArrayList<>(equipment.getTimePeriodsList());

            for (TimePeriods tp : periods) {
                boolean overlaps =
                    tp.getStartDate().isBefore(end)
                        && tp.getEndDate().isAfter(start);

                if (overlaps && tp.getPeriodType() == PeriodType.RENTED) {
                    equipment.getTimePeriodsList().remove(tp);
                }
            }
        }
    }


}
