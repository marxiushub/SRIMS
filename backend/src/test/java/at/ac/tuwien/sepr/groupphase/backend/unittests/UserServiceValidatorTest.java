package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.CustomerCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.CustomerUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.StaffUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.UserServiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceValidatorTest {

    private UserServiceValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new UserServiceValidator();
    }

    @Test
    public void userCreationDtoValidator_withNullDto_throwsValidationException() {
        assertThrows(
            ValidationException.class,
            () -> validator.userCreationDtoValidator(null)
        );
    }

    @Test
    public void userCreationDtoValidator_withBlankEmail_throwsValidationException() {
        CustomerCreationDto dto = new CustomerCreationDto();
        dto.setEmail("   ");

        assertThrows(
            ValidationException.class,
            () -> validator.userCreationDtoValidator(dto)
        );
    }

    @Test
    public void userCreationDtoValidator_withValidEmail_doesNotThrow() {
        CustomerCreationDto dto = new CustomerCreationDto();
        dto.setEmail("valid@test.at");

        assertDoesNotThrow(() -> validator.userCreationDtoValidator(dto));
    }

    @Test
    public void userUpdateDtoValidator_withEmptyCustomerDto_throwsValidationException() {
        CustomerUpdateDto dto = new CustomerUpdateDto();

        assertThrows(
            ValidationException.class,
            () -> validator.userUpdateDtoValidator(dto)
        );
    }

    @Test
    public void userUpdateDtoValidator_withEmptyStaffDto_throwsValidationException() {
        StaffUpdateDto dto = new StaffUpdateDto();

        assertThrows(
            ValidationException.class,
            () -> validator.userUpdateDtoValidator(dto)
        );
    }

    @Test
    public void userUpdateDtoValidator_withOnlyFirstName_doesNotThrow() {
        CustomerUpdateDto dto = new CustomerUpdateDto();
        dto.setFirstName("Max");

        assertDoesNotThrow(() -> validator.userUpdateDtoValidator(dto));
    }

    @Test
    public void userUpdateDtoValidator_withOnlyLastName_doesNotThrow() {
        CustomerUpdateDto dto = new CustomerUpdateDto();
        dto.setLastName("Mustermann");

        assertDoesNotThrow(() -> validator.userUpdateDtoValidator(dto));
    }

    @Test
    public void userUpdateDtoValidator_withUserName_doesNotThrow() {
        StaffUpdateDto dto = new StaffUpdateDto();
        dto.setUserName("updated_staff");

        assertDoesNotThrow(() -> validator.userUpdateDtoValidator(dto));
    }

    @Test
    public void idTester_withNullId_throwsIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.idTester(null)
        );
    }

    @Test
    public void idTester_withNegativeId_throwsIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> validator.idTester(-1L)
        );
    }

    @Test
    public void idTester_withValidId_doesNotThrow() {
        assertDoesNotThrow(() -> validator.idTester(1L));
    }
}
