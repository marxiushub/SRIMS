package at.ac.tuwien.sepr.groupphase.backend.service.impl;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.TimePeriods;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.exception.LocalizedError;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReservationValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EquipmentRepository equipmentRepository;
    private final ReservationRepository reservationRepository;
    private final CustomerProfileRepository customerProfileRepository;

    @Autowired
    public ReservationValidator(EquipmentRepository equipmentRepository, ReservationRepository reservationRepository, CustomerProfileRepository customerProfileRepository) {
        this.equipmentRepository = equipmentRepository;
        this.reservationRepository = reservationRepository;
        this.customerProfileRepository = customerProfileRepository;
    }

    public void validateCreateDto(ReservationCreationDto dto) {
        if (dto == null) {
            throw new ValidationException("Reservation creation dto must not be null", "Dto zur Erstellung der Reservierung ist NULL",
                List.of(new LocalizedError("dto is null", "DTO ist null")));
        }

        List<LocalizedError> validationErrors = new ArrayList<>();

        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            validationErrors.add(new LocalizedError("End date is before start date", "End-Datum ist vor Start-Datum"));
        }

        if (dto.getCustomerProfileId() == null || !customerProfileRepository.existsById(dto.getCustomerProfileId())) {
            validationErrors.add(new LocalizedError("No such CustomerProfile with id: " + dto.getCustomerProfileId(),
                "Es gibt kein Kunden Profil mit ID: " + dto.getCustomerProfileId()));
        }

        if (dto.getEquipmentIds() != null && !dto.getEquipmentIds().isEmpty()) {
            validateEquipmentList(dto.getEquipmentIds(), validationErrors);

            if (validationErrors.isEmpty()) {
                List<Equipment> equipments = equipmentRepository.findAllByIdsLocked(dto.getEquipmentIds());
                for (Equipment equipment : equipments) {
                    isEquipmentAvailable(equipment, dto.getStartDate(), dto.getEndDate(), null, validationErrors);
                }
            }
        } else {
            validationErrors.add(new LocalizedError("A reservation must contain at least one equipment.",
                "Eine Reservierung muss mindestens 1 Equipment enthalten."));
        }

        //Status should be created

        if (dto.getReservationStatus() == null) {
            validationErrors.add(new LocalizedError("Reservation status must not be null", "Der Reservieruns-Status darf nicht null sein"));
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for reservation creation", "Validation für Erstellung der Reservierung fehlgeschlagen", validationErrors);
        }
    }

    public void validateUpdateDto(ReservationUpdateDto dto, Long userId) {
        if (dto == null) {
            throw new ValidationException("Reservation update dto must not be null", "Das Reservierungs-Update-DTO darf nicht null sein",
                List.of(new LocalizedError("dto is null", "DTO ist null")));
        }

        List<LocalizedError> validationErrors = new ArrayList<>();

        Reservation reservation = reservationRepository.findByIdLocked(dto.getId()).orElse(null);
        if (reservation == null) {
            validationErrors.add(new LocalizedError("No such reservation with id " + dto.getId(),
                "Es gibt keine Reservation mit ID " + dto.getId()));
            throw new ValidationException("Validation failed", "Validation fehlgeschlagen", validationErrors);
        }

        if (userId != null) {
            if (!Objects.equals(reservation.getCustomerProfile().getCustomer().getId(), userId)) {
                validationErrors.add(new LocalizedError("User is not the owner of this reservation", "User ist nicht Besitzer der Reservation"));
            } else {
                LocalDate nowPlusTwoDays = LocalDate.now().plusDays(2);
                if (reservation.getStartDate().isBefore(nowPlusTwoDays)) {
                    validationErrors.add(new LocalizedError("Reservation can no longer be changed within two days of its start date",
                        "Reservierung kann innerhalb von 2 Tagen zum Start-Datum nicht mehr geändert werden"));
                }
                if (dto.getCustomerProfileId() != null && reservation.getCustomerProfile().getCustomer().getProfiles().stream().noneMatch(profile -> profile.getId().equals(dto.getCustomerProfileId()))) {
                    validationErrors.add(new LocalizedError("Customer profile does not belong to the customer",
                        "Kunden Profil gehört nicht zu diesem Kunden"));
                }
            }
            if (dto.getReservationStatus() != null) {
                validationErrors.add(new LocalizedError("Reservation status must not be changed by the customer",
                    "Reservierungs-Status darf nicht von Kunden verändert werden"));
            }
        }

        LocalDate pickUpDate = dto.getStartDate() != null ? dto.getStartDate() : reservation.getStartDate();
        LocalDate returnDate = dto.getEndDate() != null ? dto.getEndDate() : reservation.getEndDate();

        if (returnDate.isBefore(pickUpDate)) {
            validationErrors.add(new LocalizedError("End date is before start date", "End-Datum ist vor Start-Datum"));
        }

        if (dto.getCustomerProfileId() != null && !customerProfileRepository.existsById(dto.getCustomerProfileId())) {
            validationErrors.add(new LocalizedError("No such CustomerProfile with id: " + dto.getCustomerProfileId(),
                "Es gibt kein Kunden Profil mit ID: " + dto.getCustomerProfileId()));
        }

        if (dto.getEquipmentIds() != null) {
            if (dto.getEquipmentIds().isEmpty()) {
                validationErrors.add(new LocalizedError("A reservation must contain at least one equipment.",
                    "Eine Reservierung muss mindestens 1 Equipment enthalten"));
            } else {
                validateEquipmentList(dto.getEquipmentIds(), validationErrors);

                if (validationErrors.isEmpty()) {
                    List<Equipment> equipments = equipmentRepository.findAllByIdsLocked(dto.getEquipmentIds());
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
            throw new ValidationException("Validation of the dto for update failed", "Validierung des DTOs zur Aktualisierung fehlgeschlagen", validationErrors);
        }
    }

    public void validateReservationAddEquip(ReservationAddDeleteEquipmentDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException("dto must not be null");
        }

        List<LocalizedError> validationErrors = new ArrayList<>();

        Reservation reservation = reservationRepository.findByIdLocked(dto.getId()).orElseThrow(() ->
            new NotFoundException("Reservation with ID " + dto.getId() + " not found.")
        );

        validateNoDuplicateEquipmentInRequestOrReservation(dto.getEquipmentIds(), reservation, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for adding equipment", validationErrors);
        }

        if (dto.getEquipmentIds() != null) {

            validateEquipmentList(dto.getEquipmentIds(), validationErrors);

            if (validationErrors.isEmpty()) {
                List<Equipment> equipmentList = equipmentRepository.findAllByIdsLocked(dto.getEquipmentIds());
                for (Equipment equipment : equipmentList) {
                    isEquipmentAvailable(equipment, reservation.getStartDate(), reservation.getEndDate(), null, validationErrors);
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(
                "Validation failed for adding equipment",
                "Validierung beim Hinzufügen der Ausrüstung fehlgeschlagen",
                validationErrors
            );
        }
    }

    public void validateReservationRemoveEquipment(ReservationAddDeleteEquipmentDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException("dto must not be null");
        }

        List<LocalizedError> validationErrors = new ArrayList<>();

        Reservation reservation = reservationRepository.findByIdLocked(dto.getId()).orElseThrow(() ->
            new NotFoundException("Reservation with ID " + dto.getId() + " not found.")
        );

        if (dto.getEquipmentIds() != null && !dto.getEquipmentIds().isEmpty()) {
            validateEquipmentList(dto.getEquipmentIds(), validationErrors);
            for (Long equipmentId : dto.getEquipmentIds()) {
                boolean equipmentInReservation = reservation.getItems().stream()
                    .anyMatch(item -> item.getEquipment().getId().equals(equipmentId));

                if (!equipmentInReservation) {
                    validationErrors.add(new LocalizedError("Equipment with ID " + equipmentId + " is not part of this reservation",
                        "Equipment mit ID " + equipmentId + " ist nicht Teil dieser Reservation"));
                }
            }
        }

        validateReservationNotEmptyAfterRemove(dto.getEquipmentIds(), reservation, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of the dto for update failed", "Validierung des DTOs zur Aktualisierung fehlgeschlagen", validationErrors);
        }

    }


    private void isEquipmentAvailable(Equipment equipment, LocalDate start, LocalDate end, Long reservationIdToIgnore, List<LocalizedError> validationErrors) {
        for (TimePeriods time : equipment.getTimePeriodsList()) {
            if (reservationIdToIgnore != null && time.getReservation() != null && time.getReservation().getId().equals(reservationIdToIgnore)) {
                continue;
            }

            if (time.getStartDate().isBefore(end) && time.getEndDate().isAfter(start)) {
                if (time.getPeriodType() == PeriodType.RENTED) {
                    validationErrors.add(new LocalizedError("Equipment with ID " + equipment.getId() + " is already reserved in this time range",
                        "Equipment mit ID " + equipment.getId() + " ist in diesem Zeitraum bereits reserviert"));
                } else {
                    validationErrors.add(new LocalizedError("Equipment with ID " + equipment.getId() + " is not available at this date",
                        "Equipment mit ID " + equipment.getId() + " ist nicht verfügbar in diesem Zeitraum"));
                }
            } else if (time.getEndDate().isEqual(start)) {
                validationErrors.add(new LocalizedError("Equipment with ID " + equipment.getId() + " is not available at this date", "Equipment mit Id " + equipment.getId()
                    + " ist an diesem Datum nicht verfügbar"));
            }
        }
    }

    private void validateEquipmentList(List<Long> equipmentList, List<LocalizedError> validationErrors) {
        Set<Long> buffer = new HashSet<>();
        for (Long equipId : equipmentList) {
            if (!buffer.add(equipId)) {
                validationErrors.add(new LocalizedError("equipment with id: " + equipId + "is double in list",
                    "Equipment mit ID: " + equipId + "ist doppelt in Liste"));
            }
            if (!equipmentRepository.existsById(equipId)) {
                validationErrors.add(new LocalizedError("equipment from updateList does not exists",
                    "Equipment von updateList existiert nicht"));
            }
        }

    }

    private void validateNoDuplicateEquipmentInRequestOrReservation(
        List<Long> requestedEquipmentIds,
        Reservation reservation,
        List<LocalizedError> validationErrors
    ) {
        if (requestedEquipmentIds == null || requestedEquipmentIds.isEmpty()) {
            return;
        }

        Set<Long> seenIdsInRequest = new HashSet<>();
        Set<Long> duplicateInRequest = new HashSet<>();
        Set<Long> alreadyInReservation = new HashSet<>();

        Set<Long> existingEquipmentIds = reservation.getItems().stream()
            .map(item -> item.getEquipment().getId())
            .collect(Collectors.toSet());

        for (Long id : requestedEquipmentIds) {
            if (!seenIdsInRequest.add(id)) {
                duplicateInRequest.add(id);
            }

            if (existingEquipmentIds.contains(id)) {
                alreadyInReservation.add(id);
            }
        }

        if (!duplicateInRequest.isEmpty()) {
            validationErrors.add(new LocalizedError("Equipment list contains duplicate IDs: " + duplicateInRequest, "Equipment Liste enthält doppelte IDs: " + duplicateInRequest));
        }

        if (!alreadyInReservation.isEmpty()) {
            validationErrors.add(new LocalizedError("The following equipments are already part of this reservation: " + alreadyInReservation,
                "Die folgenden Equipments sind bereits Teil dieser Reservierung: " + alreadyInReservation));
        }
    }

    private void validateReservationNotEmptyAfterRemove(
        List<Long> equipmentIdsToRemove,
        Reservation reservation,
        List<LocalizedError> validationErrors
    ) {
        if (equipmentIdsToRemove == null || equipmentIdsToRemove.isEmpty()) {
            return;
        }

        long validItemsToRemove = equipmentIdsToRemove.stream()
            .distinct()
            .filter(id -> reservation.getItems().stream()
                .anyMatch(item -> item.getEquipment().getId().equals(id)))
            .count();

        if (reservation.getItems().size() <= validItemsToRemove) {
            validationErrors.add(new LocalizedError("Cannot remove all items. A reservation must contain at least one equipment.",
                "Es könnten nicht alle Equipments entfernt werden. Eine Reservation muss mindestens 1 Equipment enthalten."));
        }
    }

}
