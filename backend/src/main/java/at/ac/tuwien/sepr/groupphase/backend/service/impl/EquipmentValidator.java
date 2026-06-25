package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
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

        List<String> errors = new ArrayList<>();

        if (price <= 0) {
            errors.add("Price must be greater than 0.");
        }

        if (model == null || model.isBlank()) {
            errors.add("Model must not be empty.");
        } else if (model.length() > 100) {
            errors.add("Model must not exceed 100 characters.");
        }

        if (status == null) {
            errors.add("Status must not be null.");
        }

        if (creationNumber <= 0 || creationNumber > 100) {
            errors.add("Creation number must be between 1 and 100.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Equipment creation validation failed", errors);
        }
    }

    // =========================
    // UPDATE
    // =========================

    public void validateUpdate(Equipment equipment,
                               Double price,
                               String model,
                               RentalStatus status) {

        List<String> errors = new ArrayList<>();

        if (equipment == null) {
            throw new IllegalArgumentException("Equipment is null");
        }

        if (hasBlockingReservation(equipment)) {
            errors.add("Equipment is currently reserved and cannot be updated.");
        }

        if (price != null && price < 0) {
            errors.add("Price must not be negative.");
        }

        if (model != null && model.isBlank()) {
            errors.add("Model must not be blank.");
        } else if (model != null && model.length() > 100) {
            errors.add("Model must not exceed 100 characters.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Equipment update validation failed", errors);
        }
    }

    // =========================
    // DELETE
    // =========================

    public void validateDeletable(Equipment equipment) {

        List<String> errors = new ArrayList<>();

        if (equipment == null) {
            throw new IllegalArgumentException("Equipment is null");
        }

        if (hasBlockingReservation(equipment)) {
            errors.add("Equipment is currently reserved and cannot be deleted.");
        }

        if (equipment.getStatus() == RentalStatus.RENTED) {
            errors.add("Equipment is currently rented and cannot be deleted.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Equipment deletion validation failed", errors);
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