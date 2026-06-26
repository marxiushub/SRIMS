package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.LocalizedError;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;


/**
 * Validator for customer profile-related operations.
 */
@Component
public class CustomerProfileValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void validateUpdateDto(CustomerProfileUpdateDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException("CustomerProfileUpdateDto is null");
        }

        List<LocalizedError> errors = new ArrayList<>();

        boolean hasAnyField =
            (dto.getProfileName() != null && !dto.getProfileName().isBlank())
                || dto.getHeight() != null
                || dto.getWeight() != null
                || dto.getShoeSize() != null
                || dto.getSkillLevel() != null;

        if (!hasAnyField) {
            errors.add(new LocalizedError("At least one field must be provided for update", "Mindestens ein Feld muss im Update verändert werden"));
        }

        if (dto.getProfileName() != null) {

            String profileName = dto.getProfileName().trim();

            if (profileName.isEmpty()) {
                errors.add(new LocalizedError("Profile name must not be blank", "Der Profil Name darf nicht leer sein"));
            }

            if (profileName.length() > 100) {
                errors.add(new LocalizedError("Profile name must not exceed 100 characters", "Der Profil Name darf nicht über 100 Zeichen lang sein"));
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(
                "Validation of customer profile update failed",
                "Validierung der Kundenprofil-Aktualisierung fehlgeschlagen",
                errors
            );
        }
    }

    public void validateCreationDto(CustomerProfileCreationDto dto) {

        LOGGER.trace("Validating customer profile creation DTO");

        if (dto == null) {
            throw new IllegalArgumentException("CustomerProfileCreationDto is null");
        }

        List<LocalizedError> errors = new ArrayList<>();

        if (dto.getProfileName() != null) {

            String profileName = dto.getProfileName().trim();

            if (profileName.isEmpty()) {
                errors.add(new LocalizedError("Profile name must not be blank", "Der Profil Name darf nicht leer sein"));
            }

            if (profileName.length() > 100) {
                errors.add(new LocalizedError("Profile name must not exceed 100 characters",
                    "Der Profil Name darf nicht länger als 100 Zeichen lang sein"));
            }
        } else {
            errors.add(new LocalizedError("Profile name must not be blank", "Der Profil Name darf nicht leer sein"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(
                "Validation of customer profile creation failed",
                "Validierung der Kundenprofil-Erstellung fehlgeschlagen.",
                errors
            );
        }
    }

}
