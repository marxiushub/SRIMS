package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ReservationMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationServiceImpl implements at.ac.tuwien.sepr.groupphase.backend.service.ReservationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ReservationMapper mapper;
    private final ReservationValidator validator;
    private final ReservationRepository reservationRepository;
    private final EquipmentRepository equipmentRepository;
    private final CustomerProfileRepository customerProfileRepository;

    @Autowired
    public ReservationServiceImpl(ReservationMapper reservationMapper,
                                  ReservationRepository reservationRepository,
                                  EquipmentRepository equipmentRepository,
                                  CustomerProfileRepository customerProfileRepository,
                                  ReservationValidator validator) {
        this.mapper = reservationMapper;
        this.reservationRepository = reservationRepository;
        this.equipmentRepository = equipmentRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.validator = validator;
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

        return mapper.entityToDetailDto(reservation);
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
            LocalDate dropOffDate =  pickUpDate.plusDays(dto.getRentDurationDays());

            validator.isEquipmentAvailable(equipment, pickUpDate, dropOffDate);
            reservation.addItem(equipment);

            equipment.addTimePeriod(pickUpDate, dropOffDate, PeriodType.RENTED);
        }

        Reservation savedReservation = reservationRepository.save(reservation);
        //bestätigungs-email senden
        return mapper.entityToDetailDto(savedReservation);
    }

    @Override
    public void deleteReservation(Long id) {
        LOGGER.trace("Deleting reservation with id {}", id);

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        if (id < 0) {
            throw new IllegalArgumentException("id is negative");
        }

        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("Reservation with ID " + id + " was not found.");
        }

        //bestätigungs-mail senden

        reservationRepository.deleteById(id);

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

        if (dto.getPickUpTime() != null) {
            reservation.setPickUpTime(dto.getPickUpTime());
        }

        if (dto.getPickUpDate() != null) {
            reservation.setPickUpDate(dto.getPickUpDate());
        }

        if (dto.getRentDurationDays() != null) {
            reservation.setRentDurationDays(dto.getRentDurationDays());
        }

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

        /*
         * equipment
         */
        if (dto.getEquipmentIds() != null) {

            reservation.getItems().clear();

            List<Equipment> equipmentList =
                equipmentRepository.findAllById(dto.getEquipmentIds());

            for (Equipment equipment : equipmentList) {
                reservation.addItem(equipment);
            }
        }

        Reservation saved = reservationRepository.save(reservation);
        return mapper.entityToDetailDto(saved);
    }

    @Override
    public List<ReservationDetailDto> searchReservations(ReservationSearchDto searchDto) {
        return List.of();
    }

    @Override
    @Transactional
    public ReservationDetailDto addEquipmentToReservation(ReservationUpdateDto dto) {
        validator.validateUpdateDto(dto);

        Reservation reservation = reservationRepository.findById(dto.getId()).orElse(null);

        List<Equipment> equipmentList =
            equipmentRepository.findAllById(dto.getEquipmentIds());

        for (Equipment equipment : equipmentList) {
            reservation.addItem(equipment);
        }

        return mapper.entityToDetailDto(reservation);
    }

    @Override
    public ReservationDetailDto removeEquipmentFromReservation(List<Equipment> equipments, Long reservationId) {
        return null;
    }
}
