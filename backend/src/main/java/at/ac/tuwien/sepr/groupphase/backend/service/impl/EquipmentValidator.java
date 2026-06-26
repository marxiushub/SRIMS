package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.LocalizedError;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class EquipmentValidator {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // =========================
    // CREATE
    // =========================

    public void validateCreate(double price,
                               String model,
                               RentalStatus status,
                               int creationNumber) {

        List<LocalizedError> errors = new ArrayList<>();

        if (price <= 0) {
            errors.add(new LocalizedError("Price must be greater than 0.", "Der Preis muss größer als 0 sein."));
        }

        if (model == null || model.isBlank()) {
            errors.add(new LocalizedError("Model must not be empty.", "Modell darf nicht leer sein."));
        } else if (model.length() > 100) {
            errors.add(new LocalizedError("Model must not exceed 100 characters.", "Modell darf nicht länger als 100 Zeichen sein."));
        }

        if (status == null) {
            errors.add(new LocalizedError("Status must not be null.", "Status darf nicht null sein."));
        }

        if (creationNumber <= 0 || creationNumber > 100) {
            errors.add(new LocalizedError("Creation number must be between 1 and 100.", "Creation number muss zwischen 1 und 100 sein."));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Equipment creation validation failed", "Validierung der Ausrüstungserstellung fehlgeschlagen", errors);
        }
    }

    // =========================
    // UPDATE
    // =========================

    public void validateUpdate(Equipment equipment,
                               Double price,
                               String model,
                               RentalStatus status) {

        List<LocalizedError> errors = new ArrayList<>();

        if (equipment == null) {
            throw new IllegalArgumentException("Equipment is null");
        }

        if (hasBlockingReservation(equipment)) {
            errors.add(new LocalizedError("Equipment is currently reserved and cannot be updated.",
                "Equipment ist derzeit reserviert und kann nicht geupdated werden"));
        }

        if (price != null && price < 0) {
            errors.add(new LocalizedError("Price must not be negative.", "Preis darf nicht negativ sein."));
        }

        if (model != null && model.isBlank()) {
            errors.add(new LocalizedError("Model must not be blank.", "Modell darf nicht leer sein."));
        } else if (model != null && model.length() > 100) {
            errors.add(new LocalizedError("Model must not exceed 100 characters.", "Modell darf nicht länger als 100 Zeichen lang sein"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Equipment update validation failed", "Update des Equipments fehlgeschlagen", errors);
        }
    }

    // =========================
    // DELETE
    // =========================

    public void validateDeletable(Equipment equipment) {

        List<LocalizedError> errors = new ArrayList<>();

        if (equipment == null) {
            throw new IllegalArgumentException("Equipment is null");
        }

        if (hasBlockingReservation(equipment)) {
            errors.add(new LocalizedError("Equipment is currently reserved and cannot be deleted.",
                "Equipment ist derzeit reserviert und kann nicht gelöscht werden."));
        }

        if (equipment.getStatus() == RentalStatus.RENTED) {
            errors.add(new LocalizedError("Equipment is currently rented and cannot be deleted.",
                "Equipment ist derzeit verliehen und kann nicht gelöscht werden."));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Equipment deletion validation failed", "Validierung der Ausrüstungs-Löschung fehlgeschlagen", errors);
        }
    }


    //Helper methods
    //True when the given Equipment is in an active reservation
    private boolean hasBlockingReservation(Equipment equipment) {

        LocalDate today = LocalDate.now();

        return equipment.getTimePeriodsList().stream()
            .anyMatch(tp -> (
                !tp.getStartDate().isAfter(today)
                    && !tp.getEndDate().isBefore(today)
                )
                || tp.getStartDate().isAfter(today)
            );
    }
}