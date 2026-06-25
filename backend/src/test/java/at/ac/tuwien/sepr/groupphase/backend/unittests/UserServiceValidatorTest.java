package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.CustomerCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.search.CustomerSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.CustomerUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.StaffUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.UserServiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceValidatorTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceValidator validator;

    @BeforeEach
    void setup() {
        validator = new UserServiceValidator(userRepository);
    }

    // =========================
    // ID TESTS
    // =========================

    @Test
    void idTester_withNullId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> validator.idTester(null));
    }

    @Test
    void idTester_withNegativeId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> validator.idTester(-1L));
    }

    @Test
    void idTester_withValidId_doesNotThrow() {
        assertDoesNotThrow(() -> validator.idTester(1L));
    }

    // =========================
    // CREATION VALIDATION
    // =========================

    @Test
    void userCreationDtoValidator_withNullDto_throwsValidation() {
        assertThrows(ValidationException.class,
            () -> validator.userCreationDtoValidator(null));
    }

    @Test
    void userCreationDtoValidator_withBlankEmail_throwsValidation() {
        CustomerCreationDto dto = new CustomerCreationDto();
        dto.setEmail("");

        assertThrows(ValidationException.class,
            () -> validator.userCreationDtoValidator(dto));
    }

    @Test
    void userCreationDtoValidator_withNullEmail_throwsValidation() {
        CustomerCreationDto dto = new CustomerCreationDto();
        dto.setEmail(null);

        assertThrows(ValidationException.class,
            () -> validator.userCreationDtoValidator(dto));
    }

    @Test
    void userCreationDtoValidator_withAlreadyRegisteredEmail_throwsValidation() {
        CustomerCreationDto dto = new CustomerCreationDto();
        dto.setEmail("taken@example.com");

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(ValidationException.class,
            () -> validator.userCreationDtoValidator(dto));
    }

    @Test
    void userCreationDtoValidator_withNewEmail_doesNotThrow() {
        CustomerCreationDto dto = new CustomerCreationDto();
        dto.setEmail("new@example.com");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        assertDoesNotThrow(() -> validator.userCreationDtoValidator(dto));
    }

    // =========================
    // UPDATE VALIDATION
    // =========================

    @Test
    void userUpdateDtoValidator_withAllFieldsNull_throwsValidation() {
        CustomerUpdateDto dto = new CustomerUpdateDto();

        assertThrows(ValidationException.class,
            () -> validator.userUpdateDtoValidator(1L, dto));
    }

    @Test
    void userUpdateDtoValidator_withOnlyFirstName_doesNotThrow() {
        CustomerUpdateDto dto = new CustomerUpdateDto();
        dto.setFirstName("Max");

        assertDoesNotThrow(() -> validator.userUpdateDtoValidator(1L, dto));
    }

    @Test
    void userUpdateDtoValidator_withOnlyLastName_doesNotThrow() {
        CustomerUpdateDto dto = new CustomerUpdateDto();
        dto.setLastName("Mustermann");

        assertDoesNotThrow(() -> validator.userUpdateDtoValidator(1L, dto));
    }

    @Test
    void userUpdateDtoValidator_withEmail_doesNotThrow_whenUnique() {
        CustomerUpdateDto dto = new CustomerUpdateDto();
        dto.setEmail("new@example.com");

        when(userRepository.findUserByEmail("new@example.com"))
            .thenReturn(java.util.Optional.empty());

        assertDoesNotThrow(() -> validator.userUpdateDtoValidator(1L, dto));
    }

    @Test
    void userUpdateDtoValidator_withEmail_conflict_throwsValidation() {
        CustomerUpdateDto dto = new CustomerUpdateDto();
        dto.setEmail("taken@example.com");

        var existingUser = new Object() {
            public Long getId() { return 2L; }
        };

        when(userRepository.findUserByEmail("taken@example.com"))
            .thenReturn(java.util.Optional.of(
                new at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser() {
                    @Override public Long getId() { return 2L; }
                }
            ));

        assertThrows(ValidationException.class,
            () -> validator.userUpdateDtoValidator(1L, dto));
    }

    // =========================
    // SEARCH VALIDATION
    // =========================

    @Test
    void customerSearchDtoValidator_withNullDto_throwsValidation() {
        assertThrows(ValidationException.class,
            () -> validator.customerSearchDtoValidator(null));
    }

    @Test
    void customerSearchDtoValidator_withAllFieldsNull_throwsValidation() {
        CustomerSearchDto dto = new CustomerSearchDto();

        assertThrows(ValidationException.class,
            () -> validator.customerSearchDtoValidator(dto));
    }

    @Test
    void customerSearchDtoValidator_withValidEmail_doesNotThrow() {
        CustomerSearchDto dto = new CustomerSearchDto();
        dto.setEmail("test@example.com");

        assertDoesNotThrow(() -> validator.customerSearchDtoValidator(dto));
    }

    @Test
    void customerSearchDtoValidator_withInvalidEmail_throwsValidation() {
        CustomerSearchDto dto = new CustomerSearchDto();
        dto.setEmail("invalid-email");

        assertThrows(ValidationException.class,
            () -> validator.customerSearchDtoValidator(dto));
    }

    @Test
    void customerSearchDtoValidator_withLongUserName_throwsValidation() {
        CustomerSearchDto dto = new CustomerSearchDto();
        dto.setUserName("x".repeat(101));

        assertThrows(ValidationException.class,
            () -> validator.customerSearchDtoValidator(dto));
    }

    @Test
    void customerSearchDtoValidator_withValidSearch_doesNotThrow() {
        CustomerSearchDto dto = new CustomerSearchDto();
        dto.setUserName("john");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");

        assertDoesNotThrow(() -> validator.customerSearchDtoValidator(dto));
    }

    @Test
    void customerSearchDtoValidator_withBlankFields_doesNotThrow() {
        CustomerSearchDto dto = new CustomerSearchDto();
        dto.setUserName("   ");
        dto.setFirstName("   ");

        assertThrows(ValidationException.class,
            () -> validator.customerSearchDtoValidator(dto));
    }
}