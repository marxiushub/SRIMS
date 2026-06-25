package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileCreationDto;
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

        List<String> errors = new ArrayList<>();

        boolean hasAnyField =
            (dto.getProfileName() != null && !dto.getProfileName().isBlank())
                || dto.getHeight() != null
                || dto.getWeight() != null
                || dto.getShoeSize() != null
                || dto.getSkillLevel() != null;

        if (!hasAnyField) {
            errors.add("At least one field must be provided for update");
        }

        if (dto.getProfileName() != null) {

            String profileName = dto.getProfileName().trim();

            if (profileName.isEmpty()) {
                errors.add("Profile name must not be blank");
            }

            if (profileName.length() > 100) {
                errors.add("Profile name must not exceed 100 characters");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(
                "Validation of customer profile update failed",
                errors
            );
        }
    }

    public void validateCreationDto(CustomerProfileCreationDto dto) {

        LOGGER.trace("Validating customer profile creation DTO");

        if (dto == null) {
            throw new IllegalArgumentException("CustomerProfileCreationDto is null");
        }

        List<String> errors = new ArrayList<>();

        if (dto.getProfileName() != null) {

            String profileName = dto.getProfileName().trim();

            if (profileName.isEmpty()) {
                errors.add("Profile name must not be blank");
            }

            if (profileName.length() > 100) {
                errors.add("Profile name must not exceed 100 characters");
            }
        } else {
            errors.add("Profile name must not be blank");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(
                "Validation of customer profile creation failed",
                errors
            );
        }
    }

}
