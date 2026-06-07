package at.ac.tuwien.sepr.groupphase.backend.service.impl;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.TimePeriods;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ReservationValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EquipmentRepository equipmentRepository;
    private final ReservationRepository reservationRepository;
    private final CustomerProfileRepository customerProfileRepository;

    @Autowired
    public ReservationValidator(  EquipmentRepository equipmentRepository, ReservationRepository reservationRepository, CustomerProfileRepository customerProfileRepository) {
        this.equipmentRepository = equipmentRepository;
        this.reservationRepository = reservationRepository;
        this.customerProfileRepository = customerProfileRepository;
    }
    public void validateCreateDto(ReservationCreationDto dto) {
        if (dto == null) {
            throw new ValidationException("Reservation creation dto must not be null", List.of("dto is null"));
        }

        List<String> validationErrors = new ArrayList<>();

        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            validationErrors.add("End date is before start date");
        }

        if (dto.getCustomerProfileId() == null || !customerProfileRepository.existsById(dto.getCustomerProfileId())) {
            validationErrors.add("No such CustomerProfile with id: " + dto.getCustomerProfileId());
        }

        if (dto.getEquipmentIds() != null && !dto.getEquipmentIds().isEmpty()) {
            validateEquipmentList(dto.getEquipmentIds(), validationErrors);

            if (validationErrors.isEmpty()) {
                List<Equipment> equipments = equipmentRepository.findAllById(dto.getEquipmentIds());
                for (Equipment equipment : equipments) {
                    isEquipmentAvailable(equipment, dto.getStartDate(), dto.getEndDate(), null, validationErrors);
                }
            }
        } else {
            validationErrors.add("A reservation must contain at least one equipment.");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for reservation creation", validationErrors);
        }
    }
    public void validateUpdateDto(ReservationUpdateDto dto) {
        if (dto == null) {
            throw new ValidationException("Reservation update dto must not be null", List.of("dto is null"));
        }

        List<String> validationErrors = new ArrayList<>();

        Reservation reservation = reservationRepository.findById(dto.getId()).orElse(null);
        if (reservation == null) {
            validationErrors.add("No such reservation with id " + dto.getId());
            throw new ValidationException("Validation failed", validationErrors);
        }

        LocalDate pickUpDate = dto.getStartDate() != null ? dto.getStartDate() : reservation.getStartDate();
        LocalDate returnDate = dto.getEndDate() != null ? dto.getEndDate() : reservation.getEndDate();

        if (returnDate.isBefore(pickUpDate)) {
            validationErrors.add("End date is before start date");
        }

        if (dto.getCustomerProfileId() != null && !customerProfileRepository.existsById(dto.getCustomerProfileId())) {
            validationErrors.add("No such CustomerProfile with id: " + dto.getCustomerProfileId());
        }

        if (dto.getEquipmentIds() != null) {
            if (dto.getEquipmentIds().isEmpty()) {
                validationErrors.add("A reservation must contain at least one equipment.");
            } else {
                validateEquipmentList(dto.getEquipmentIds(), validationErrors);

                if (validationErrors.isEmpty()) {
                    List<Equipment> equipments = equipmentRepository.findAllById(dto.getEquipmentIds());
                    for (Equipment equipment : equipments) {
                        isEquipmentAvailable(equipment, pickUpDate, returnDate, dto.getId(), validationErrors);
                    }
                }
            }
        } else if (dto.getStartDate() != null || dto.getEndDate() != null) {
            List<Equipment> currentEquipments = reservation.getItems().stream().map(item -> item.getEquipment()).toList();
            for (Equipment equipment : currentEquipments) {
                isEquipmentAvailable(equipment, pickUpDate, returnDate, dto.getId(), validationErrors);
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of the dto for update failed", validationErrors);
        }
    }

    public void validateReservationAddEquip(ReservationAddDeleteEquipmentDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException("dto must not be null");
        }

        List<String> validationErrors = new ArrayList<>();

        Reservation reservation = reservationRepository.findById(dto.getId()).orElseThrow(() ->
            new NotFoundException("Reservation with ID " + dto.getId() + " not found.")
        );

        if (dto.getEquipmentIds() != null) {
            for (Long id : dto.getEquipmentIds()) {
                Equipment equipment = equipmentRepository.findById(id)
                    .orElseThrow(() ->
                        new NotFoundException("Equipment with ID " + id + " not found.")
                   );
                isEquipmentAvailable(equipment, reservation.getStartDate(), reservation.getEndDate(), null, validationErrors); }
        }
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(
                "Validation failed for adding equipment",
                validationErrors
            );
        }
    }

    public void validateReservationRemoveEquipment(ReservationAddDeleteEquipmentDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException("dto must not be null");
        }

        List<String> validationErrors = new ArrayList<>();

        Reservation reservation = reservationRepository.findById(dto.getId()).orElseThrow(() ->
            new NotFoundException("Reservation with ID " + dto.getId() + " not found.")
        );

        if (dto.getEquipmentIds() != null && !dto.getEquipmentIds().isEmpty()) {
            validateEquipmentList(dto.getEquipmentIds(), validationErrors);
            for (Long equipmentId : dto.getEquipmentIds()) {
                boolean equipmentInReservation = reservation.getItems().stream()
                    .anyMatch(item -> item.getEquipment().getId().equals(equipmentId));

                if (!equipmentInReservation) {
                    validationErrors.add("Equipment with ID " + equipmentId + " is not part of this reservation");
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of the dto for update failed", validationErrors);
        }

    }


    private void isEquipmentAvailable(Equipment equipment, LocalDate start, LocalDate end, Long reservationIdToIgnore, List<String> validationErrors) {
        for (TimePeriods time : equipment.getTimePeriodsList()) {
            if (reservationIdToIgnore != null && time.getReservation() != null && time.getReservation().getId().equals(reservationIdToIgnore)) {
                continue;
            }

            if (time.getStartDate().isBefore(end) && time.getEndDate().isAfter(start)) {
                if (time.getPeriodType() == PeriodType.RENTED) {
                    validationErrors.add("Equipment with ID " + equipment.getId() + " is already reserved in this time range");
                } else {
                    validationErrors.add("Equipment with ID " + equipment.getId() + " is not available at this date");
                }
            }
        }
    }

    private void validateEquipmentList(List<Long> equipmentList, List<String> validationErrors) {
        Set<Long> buffer = new HashSet<>();
        for (Long equipId : equipmentList) {
            if (!buffer.add(equipId)) {
                validationErrors.add("equipment with id: " + equipId + "is double in list");
            }
            if (!equipmentRepository.existsById(equipId)) {
                validationErrors.add("equipment from updateList does not exists");
            }
        }

    }

}
