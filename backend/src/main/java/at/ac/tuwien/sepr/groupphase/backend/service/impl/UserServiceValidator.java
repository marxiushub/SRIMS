package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.CustomerCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.UserCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.search.CustomerSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.CustomerUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isAllBlank;

@Component
public class UserServiceValidator {

    private final UserRepository userRepository;

    private static final String EMAIL_REGEX =
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public UserServiceValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void userCreationDtoValidator(UserCreationDto dto) {

        List<String> errors = new ArrayList<>();

        if (dto == null) {
            errors.add("userCreationDto is null");
            throw new ValidationException(
                "Validation of the dto for creating users failed",
                "Validation des dtos zur Erstellung des Nutzers fehlgeschlagen",
                errors
            );
        }

        validateEmail(dto.getEmail(), errors);

        if (userRepository.existsByEmail(dto.getEmail())) {
            errors.add("This email address is already registered to another account.");
        }

        if (dto instanceof CustomerCreationDto customerDto) {
            validateDateOfBirth(customerDto.getDateOfBirth(), errors);
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(
                "Validation of the dto for creating users failed",
                "Validierung des DTOs zur Benutzererstellung fehlgeschlagen",
                errors
            );
        }
    }

    public void userUpdateDtoValidator(Long id, UserUpdateDto dto) {

        List<String> errors = new ArrayList<>();

        if (dto == null) {
            errors.add("userUpdateDto is null");
            throw new ValidationException(
                "Validation of the dto for updating users failed",
                "Validierung des DTOs zum Update des Nutzers fehlgeschlagen",
                errors
            );
        }

        boolean hasAnyField =
            dto.getEmail() != null
                || dto.getPassword() != null
                || dto.getUserName() != null;

        if (dto instanceof CustomerUpdateDto customerDto) {
            hasAnyField =
                hasAnyField
                    || customerDto.getFirstName() != null
                    || customerDto.getLastName() != null
                    || customerDto.getDateOfBirth() != null;

            validateDateOfBirth(customerDto.getDateOfBirth(), errors);
        }

        if (!hasAnyField) {
            errors.add("At least one field must be provided");
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {

            validateEmail(dto.getEmail(), errors);

            userRepository.findUserByEmail(dto.getEmail())
                .ifPresent(user -> {
                    if (!user.getId().equals(id)) {
                        errors.add(
                            "This email address is already registered to another account."
                        );
                    }
                });
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(
                "Validation of the dto for updating users failed",
                "Validierung des DTOs zur Benutzeraktualisierung fehlgeschlagen.",
                errors
            );
        }
    }

    public void customerSearchDtoValidator(CustomerSearchDto dto) {

        List<String> errors = new ArrayList<>();

        if (dto == null) {
            errors.add("searchDto is null");
            throw new ValidationException("Validation failed", "Validierung fehlgeschlagen", errors);
        }

        validateEmail(dto.getEmail(), errors);
        validateSearchField("userName", dto.getUserName(), errors);
        validateSearchField("firstName", dto.getFirstName(), errors);
        validateSearchField("lastName", dto.getLastName(), errors);

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation of search DTO failed", "Validierung des Such-Dtos fehlgeschlagen", errors);
        }
    }

    public void idTester(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("userId is null");
        }

        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
    }

    public void validateEmailFormat(String email, List<String> validationErrors) {

        if (email == null || email.isBlank()) {
            validationErrors.add("email is blank");
            throw new ValidationException(
                "Validation failed",
                "Validierung fehlgeschlagen",
                validationErrors
            );
        }

        if (!email.matches(EMAIL_REGEX)) {
            validationErrors.add("Invalid email format");
            throw new ValidationException(
                "Validation failed",
                "Validierung fehlgeschlagen.",
                validationErrors
            );
        }
    }

    private void validateEmail(String email, List<String> errors) {

        if (email == null || email.isBlank()) {
            errors.add("email is blank");
            return;
        }

        if (!email.matches(EMAIL_REGEX)) {
            errors.add("Invalid email format");
        }

        if (email.length() > 255) {
            errors.add("email must not exceed 255 characters");
        }
    }

    private void validateDateOfBirth(
        LocalDate dateOfBirth,
        List<String> errors
    ) {

        if (dateOfBirth == null) {
            return;
        }

        if (dateOfBirth.isAfter(LocalDate.now())) {
            errors.add("Date of birth must be in the past");
        }

        if (dateOfBirth.isBefore(LocalDate.now().minusYears(120))) {
            errors.add("Date of birth is not plausible");
        }
    }

    private void validateSearchField(String fieldName, String value, List<String> errors) {
        if (value == null) {
            return;
        }

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            return;
        }

        if (trimmed.length() > 100) {
            errors.add(fieldName + " must not exceed 100 characters");
        }
    }
}
