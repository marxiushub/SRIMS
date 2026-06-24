package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.UserCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.CustomerUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserServiceValidator {

    private final UserRepository userRepository;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public UserServiceValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void userCreationDtoValidator(UserCreationDto userCreationDto) {
        List<String> validationErrors = new ArrayList<>();

        if (userCreationDto == null) {
            validationErrors.add("userCreationDto is null");
        } else {
            if (userCreationDto.getEmail() == null || userCreationDto.getEmail().isBlank()) {
                validationErrors.add("email is blank");
            } else if (!userCreationDto.getEmail().matches(EMAIL_REGEX)) {
                validationErrors.add("Invalid email format");
            } else if (userRepository.existsByEmail(userCreationDto.getEmail())) {
                validationErrors.add("This email address is already registered to another account.");
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of the dto for creating users failed", validationErrors);
        }
    }

    public void userUpdateDtoValidator(UserUpdateDto userUpdateDto) {
        List<String> validationErrors = new ArrayList<>();

        if (userUpdateDto == null) {
            validationErrors.add("userUpdateDto is null");
        }

        if (userUpdateDto.getEmail() == null
            && userUpdateDto.getPassword() == null
            && userUpdateDto.getUserName() == null) {
            if (userUpdateDto instanceof CustomerUpdateDto customerDto) {

                if (customerDto.getFirstName() == null && customerDto.getLastName() == null) {
                    validationErrors.add("At least one field must be provided");
                }

            } else {
                validationErrors.add("At least one field must be provided");
            }
        }


        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of the dto for updating users failed", validationErrors);
        }
    }

    public void idTester(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("userId is null");
        }
        if (id < 0) {
            throw new IllegalArgumentException("id is negative");
        }

    }

    public void validateEmailFormat(String email, List<String> validationErrors) {
        if (email != null && !email.isBlank()) {
            if (!email.matches(EMAIL_REGEX)) {
                throw new ValidationException("Invalid email format", validationErrors);
            }
        }
    }
}
