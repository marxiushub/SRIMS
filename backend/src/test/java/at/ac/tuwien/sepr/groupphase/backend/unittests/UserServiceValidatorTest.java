package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.CustomerCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.CustomerUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.UserServiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    // --- idTester ---

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

    // --- userCreationDtoValidator ---

    @Test
    void userCreationDtoValidator_withNullDto_throwsValidation() {
        assertThrows(ValidationException.class, () -> validator.userCreationDtoValidator(null));
    }

    @Test
    void userCreationDtoValidator_withBlankEmail_throwsValidation() {
        CustomerCreationDto dto = new CustomerCreationDto();
        dto.setEmail("");
        assertThrows(ValidationException.class, () -> validator.userCreationDtoValidator(dto));
    }

    @Test
    void userCreationDtoValidator_withNullEmail_throwsValidation() {
        CustomerCreationDto dto = new CustomerCreationDto();
        dto.setEmail(null);
        assertThrows(ValidationException.class, () -> validator.userCreationDtoValidator(dto));
    }

    @Test
    void userCreationDtoValidator_withAlreadyRegisteredEmail_throwsValidation() {
        CustomerCreationDto dto = new CustomerCreationDto();
        dto.setEmail("taken@example.com");
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(ValidationException.class, () -> validator.userCreationDtoValidator(dto));
    }

    @Test
    void userCreationDtoValidator_withNewEmail_doesNotThrow() {
        CustomerCreationDto dto = new CustomerCreationDto();
        dto.setEmail("new@example.com");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        assertDoesNotThrow(() -> validator.userCreationDtoValidator(dto));
    }

    // --- userUpdateDtoValidator ---

    @Test
    void userUpdateDtoValidator_withAllFieldsNull_throwsValidation() {
        CustomerUpdateDto dto = new CustomerUpdateDto();  // alles null
        assertThrows(ValidationException.class, () -> validator.userUpdateDtoValidator(dto));
    }

    @Test
    void userUpdateDtoValidator_withOnlyFirstName_doesNotThrow() {
        CustomerUpdateDto dto = new CustomerUpdateDto();
        dto.setFirstName("Max");
        assertDoesNotThrow(() -> validator.userUpdateDtoValidator(dto));
    }

    @Test
    void userUpdateDtoValidator_withEmail_doesNotThrow() {
        CustomerUpdateDto dto = new CustomerUpdateDto();
        dto.setEmail("new@example.com");
        assertDoesNotThrow(() -> validator.userUpdateDtoValidator(dto));
    }
}