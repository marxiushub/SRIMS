package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.UserCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.CustomerUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.UserType;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserServiceValidator {

    public void userCreationDtoValidator(UserCreationDto userCreationDto) {
        List<String> validationErrors = new ArrayList<>();

        if (userCreationDto == null) {
            validationErrors.add("userCreationDto is null");
        } else {
            if (userCreationDto.getEmail() == null || userCreationDto.getEmail().isBlank()) {
                validationErrors.add("email is blank");
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
}
