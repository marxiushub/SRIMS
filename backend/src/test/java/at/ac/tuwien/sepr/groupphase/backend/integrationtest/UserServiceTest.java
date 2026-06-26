package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.datagenerator.DataInitializer;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.CustomerCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.StaffCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.search.CustomerSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.searchresponse.CustomerSearchResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.searchresponse.StaffSearchResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.searchresponse.UserSearchResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.CustomerUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.PasswordChangeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.StaffUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.StaffRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.AppUserDetails;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.LocalizedError;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

//Customer which are updated or deleted are created beforehand and do not use generated data
@ActiveProfiles({"test", "datagenerator"})
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataInitializer dataInitializer;

    private final List<Long> createdUserIds = new ArrayList<>();

    @BeforeEach
    public void setupSecurityContext() {

        dataInitializer.initializeData();

        Staff admin = staffRepository.findByEmail("admin@email.com")
            .orElseThrow();

        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("USER_ADMIN"),
            new SimpleGrantedAuthority("STAFF"),

            new SimpleGrantedAuthority("STAFF_READ"),
            new SimpleGrantedAuthority("STAFF_UPDATE"),
            new SimpleGrantedAuthority("STAFF_DELETE"),

            new SimpleGrantedAuthority("CUSTOMER_READ"),
            new SimpleGrantedAuthority("CUSTOMER_UPDATE"),
            new SimpleGrantedAuthority("CUSTOMER_DELETE")
        );

        AppUserDetails principal = new AppUserDetails(
            admin.getEmail(),
            admin.getPassword(),
            authorities,
            admin.getId()
        );

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(principal, null, authorities);

        authentication.setDetails(admin.getId());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    public void cleanupCreatedUsers() {
        for (Long id : createdUserIds) {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
            }
        }

        createdUserIds.clear();
        SecurityContextHolder.clearContext();
    }

    private void rememberCreatedUser(UserDetailDto createdUser) {
        createdUserIds.add(createdUser.getId());
    }

    private String uniqueEmail(String prefix) {
        return prefix + "." + UUID.randomUUID() + "@test.at";
    }

    private CustomerCreationDto validCustomerDto(String email) {
        CustomerCreationDto dto = new CustomerCreationDto();

        dto.setUserName("customer_" + UUID.randomUUID());
        dto.setPassword("Password123!");
        dto.setEmail(email);
        dto.setFirstName("Max");
        dto.setLastName("Mustermann");
        dto.setDateOfBirth(LocalDate.of(1998, 5, 10));

        return dto;
    }

    private StaffCreationDto validStaffDto(String email) {
        StaffCreationDto dto = new StaffCreationDto();

        dto.setUserName("staff_" + UUID.randomUUID());
        dto.setPassword("Password123!");
        dto.setEmail(email);

        return dto;
    }

    private static void assertContainsErrorMessage(
        ValidationException exception,
        String expectedMessage
    ) {
        assertThat(exception.getErrors())
            .extracting(LocalizedError::message)
            .contains(expectedMessage);
    }

    private static void assertContainsErrorMessageContaining(
        ValidationException exception,
        String expectedMessagePart
    ) {
        assertThat(exception.getErrors())
            .extracting(LocalizedError::message)
            .anyMatch(message -> message.contains(expectedMessagePart));
    }

    @Test
    public void createCustomer_withValidDto_returnsSavedCustomerWithId() {
        CustomerCreationDto dto = validCustomerDto(uniqueEmail("customer.create"));

        UserDetailDto created = userService.createUser(dto);
        rememberCreatedUser(created);

        ApplicationUser savedApplicationUser = userRepository.findUserByEmail(dto.getEmail()).orElseThrow();
        Customer savedCustomer = customerRepository.findById(created.getId()).orElseThrow();

        assertAll(
            "Verify that the customer is saved correctly and assigned an ID",

            () -> assertThat(created).isNotNull(),
            () -> assertThat(created.getId()).isNotNull(),
            () -> assertThat(created.getEmail()).isEqualTo(dto.getEmail()),
            () -> assertThat(created.getUserName()).isEqualTo(dto.getUserName()),

            () -> assertThat(savedApplicationUser).isInstanceOf(Customer.class),
            () -> assertThat(savedApplicationUser.getId()).isNotNull(),
            () -> assertThat(savedApplicationUser.getEmail()).isEqualTo(dto.getEmail()),
            () -> assertThat(savedApplicationUser.getUserName()).isEqualTo(dto.getUserName()),

            () -> assertThat(passwordEncoder.matches(
                dto.getPassword(),
                savedApplicationUser.getPassword()
            )).isTrue(),

            () -> assertThat(savedCustomer.getFirstName()).isEqualTo(dto.getFirstName()),
            () -> assertThat(savedCustomer.getLastName()).isEqualTo(dto.getLastName()),
            () -> assertThat(savedCustomer.getDateOfBirth()).isEqualTo(dto.getDateOfBirth()),
            () -> assertThat(customerProfileRepository.findByCustomerId(savedCustomer.getId())).isEmpty()
        );
    }

    @Test
    public void createStaff_withValidDto_returnsSavedStaffWithId() {
        StaffCreationDto dto = validStaffDto(uniqueEmail("staff.create"));

        UserDetailDto created = userService.createUser(dto);
        rememberCreatedUser(created);

        ApplicationUser saved = userRepository
            .findUserByEmail(dto.getEmail())
            .orElseThrow();

        Staff savedStaff = staffRepository
            .findById(created.getId())
            .orElseThrow();

        assertAll(
            "Verify that the staff user is saved correctly and assigned an ID",

            () -> assertThat(created).isNotNull(),
            () -> assertThat(created.getId()).isNotNull(),
            () -> assertThat(created.getEmail()).isEqualTo(dto.getEmail()),
            () -> assertThat(created.getUserName()).isEqualTo(dto.getUserName()),

            () -> assertThat(saved).isInstanceOf(Staff.class),
            () -> assertThat(saved.getId()).isNotNull(),
            () -> assertThat(saved.getEmail()).isEqualTo(dto.getEmail()),
            () -> assertThat(saved.getUserName()).isEqualTo(dto.getUserName()),

            () -> assertThat(savedStaff.getId()).isEqualTo(created.getId()),
            () -> assertThat(savedStaff.getEmail()).isEqualTo(dto.getEmail()),
            () -> assertThat(savedStaff.getUserName()).isEqualTo(dto.getUserName())
        );
    }

    @Test
    public void createUser_withExistingEmail_throwsValidationException() {
        String email = uniqueEmail("duplicate.email");

        CustomerCreationDto first = validCustomerDto(email);
        UserDetailDto created = userService.createUser(first);
        rememberCreatedUser(created);

        CustomerCreationDto duplicate = validCustomerDto(email);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.createUser(duplicate)
        );

        assertThat(exception.getMessage())
            .contains("Email is already in use");

        assertContainsErrorMessageContaining(
            exception,
            "Email " + email + " is already in use"
        );
    }

    @Test
    public void updateCustomer_withValidDto_updatesCustomerCorrectly() {
        CustomerCreationDto creationDto = validCustomerDto(uniqueEmail("customer.update"));
        UserDetailDto created = userService.createUser(creationDto);
        rememberCreatedUser(created);

        CustomerUpdateDto dto = new CustomerUpdateDto();

        dto.setUserName("updated_customer_" + UUID.randomUUID());
        dto.setPassword("NewPassword123!");
        dto.setEmail(uniqueEmail("customer.updated"));
        dto.setFirstName("UpdatedFirst");
        dto.setLastName("UpdatedLast");
        dto.setDateOfBirth(LocalDate.of(2000, 1, 1));

        UserDetailDto updated = userService.updateUser(created.getId(), dto);

        ApplicationUser savedApplicationUser = userRepository
            .findById(created.getId())
            .orElseThrow();

        Customer savedCustomer = customerRepository
            .findById(created.getId())
            .orElseThrow();
        final Long createdId = created.getId();

        assertAll(
            "Verify that the customer was updated correctly",

            () -> assertThat(updated).isNotNull(),
            () -> assertThat(updated).extracting(UserDetailDto::getId).isEqualTo(createdId),
            () -> assertThat(updated.getUserName()).isEqualTo(dto.getUserName()),
            () -> assertThat(updated.getEmail()).isEqualTo(dto.getEmail()),

            () -> assertThat(savedApplicationUser.getUserName()).isEqualTo(dto.getUserName()),
            () -> assertThat(savedApplicationUser.getEmail()).isEqualTo(dto.getEmail()),

            () -> assertThat(passwordEncoder.matches(
                dto.getPassword(),
                savedApplicationUser.getPassword()
            )).isTrue(),

            () -> assertThat(savedCustomer.getFirstName()).isEqualTo(dto.getFirstName()),
            () -> assertThat(savedCustomer.getLastName()).isEqualTo(dto.getLastName()),
            () -> assertThat(savedCustomer.getDateOfBirth()).isEqualTo(dto.getDateOfBirth())
        );
    }

    @Test
    public void updateStaff_withValidDto_updatesStaffCorrectly() {
        StaffCreationDto creationDto = validStaffDto(uniqueEmail("staff.update"));
        UserDetailDto created = userService.createUser(creationDto);
        rememberCreatedUser(created);

        StaffUpdateDto dto = new StaffUpdateDto();

        dto.setUserName("updated_staff_" + UUID.randomUUID());
        dto.setPassword("UpdatedPassword123!");
        dto.setEmail(uniqueEmail("staff.updated"));

        UserDetailDto updated = userService.updateUser(created.getId(), dto);

        ApplicationUser savedApplicationUser = userRepository
            .findById(created.getId())
            .orElseThrow();

        Staff savedStaff = staffRepository
            .findById(created.getId())
            .orElseThrow();
        final Long createdId = created.getId();

        assertAll(
            "Verify that the staff user was updated correctly",

            () -> assertThat(updated).isNotNull(),
            () -> assertThat(updated).extracting(UserDetailDto::getId).isEqualTo(createdId),
            () -> assertThat(updated.getUserName()).isEqualTo(dto.getUserName()),
            () -> assertThat(updated.getEmail()).isEqualTo(dto.getEmail()),

            () -> assertThat(savedStaff.getUserName()).isEqualTo(dto.getUserName()),
            () -> assertThat(savedStaff.getEmail()).isEqualTo(dto.getEmail()),

            () -> assertThat(passwordEncoder.matches(
                dto.getPassword(),
                savedApplicationUser.getPassword()
            )).isTrue()
        );
    }

    @Test
    public void changePassword_withValidDto_updatesPasswordAndReturnsUserDetail() {
        CustomerCreationDto creationDto = validCustomerDto(uniqueEmail("customer.password"));
        UserDetailDto created = userService.createUser(creationDto);
        rememberCreatedUser(created);

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setOldPassword(creationDto.getPassword());
        dto.setNewPassword("NewPassword123!");

        UserDetailDto updated = userService.changePassword(created.getId(), dto);

        ApplicationUser saved = userRepository.findById(created.getId()).orElseThrow();
        final Long createdId = created.getId();

        assertAll(
            "Verify that the password was changed successfully",
            () -> assertThat(updated).isNotNull(),
            () -> assertThat(updated).extracting(UserDetailDto::getId).isEqualTo(createdId),
            () -> assertThat(passwordEncoder.matches(dto.getNewPassword(), saved.getPassword())).isTrue(),
            () -> assertThat(passwordEncoder.matches(dto.getOldPassword(), saved.getPassword())).isFalse()
        );
    }

    @Test
    public void changePassword_withWrongOldPassword_throwsValidationException() {
        CustomerCreationDto creationDto = validCustomerDto(uniqueEmail("customer.password.wrongold"));
        UserDetailDto created = userService.createUser(creationDto);
        rememberCreatedUser(created);

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setOldPassword("DefinitelyWrongOldPassword123!");
        dto.setNewPassword("NewPassword123!");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.changePassword(created.getId(), dto)
        );

        assertThat(exception.getMessage()).contains("Old password is incorrect");
    }

    @Test
    public void changePassword_withSameNewPassword_throwsValidationException() {
        StaffCreationDto creationDto = validStaffDto(uniqueEmail("staff.password.same"));
        UserDetailDto created = userService.createUser(creationDto);
        rememberCreatedUser(created);

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setOldPassword(creationDto.getPassword());
        dto.setNewPassword(creationDto.getPassword());

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.changePassword(created.getId(), dto)
        );

        assertThat(exception.getMessage()).contains("New Password must differ from the current password.");
    }

    @Test
    public void changePassword_withNullDto_throwsValidationException() {
        CustomerCreationDto creationDto = validCustomerDto(uniqueEmail("customer.password.null"));
        UserDetailDto created = userService.createUser(creationDto);
        rememberCreatedUser(created);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.changePassword(created.getId(), null)
        );

        assertThat(exception.getMessage())
            .contains("Validation of the dto for changing passwords failed");

        assertContainsErrorMessage(exception, "passwordChangeDto is null");
    }

    @Test
    public void changePassword_withNullOldPassword_throwsValidationException() {
        CustomerCreationDto creationDto = validCustomerDto(uniqueEmail("customer.password.nullold"));
        UserDetailDto created = userService.createUser(creationDto);
        rememberCreatedUser(created);

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setOldPassword(null);
        dto.setNewPassword("NewPassword123!");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.changePassword(created.getId(), dto)
        );

        assertThat(exception.getMessage())
            .contains("Validation of the dto for changing passwords failed");

        assertContainsErrorMessage(exception, "oldPassword is blank");
    }

    @Test
    public void changePassword_withBlankOldPassword_throwsValidationException() {
        CustomerCreationDto creationDto = validCustomerDto(uniqueEmail("customer.password.blankold"));
        UserDetailDto created = userService.createUser(creationDto);
        rememberCreatedUser(created);

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setOldPassword("   ");
        dto.setNewPassword("NewPassword123!");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.changePassword(created.getId(), dto)
        );

        assertThat(exception.getMessage())
            .contains("Validation of the dto for changing passwords failed");

        assertContainsErrorMessage(exception, "oldPassword is blank");
    }

    @Test
    public void changePassword_withNullNewPassword_throwsValidationException() {
        CustomerCreationDto creationDto = validCustomerDto(uniqueEmail("customer.password.nullnew"));
        UserDetailDto created = userService.createUser(creationDto);
        rememberCreatedUser(created);

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setOldPassword(creationDto.getPassword());
        dto.setNewPassword(null);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.changePassword(created.getId(), dto)
        );

        assertThat(exception.getMessage())
            .contains("Validation of the dto for changing passwords failed");

        assertContainsErrorMessage(exception, "newPassword is blank");
    }

    @Test
    public void changePassword_withBlankNewPassword_throwsValidationException() {
        CustomerCreationDto creationDto = validCustomerDto(uniqueEmail("customer.password.blanknew"));
        UserDetailDto created = userService.createUser(creationDto);
        rememberCreatedUser(created);

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setOldPassword(creationDto.getPassword());
        dto.setNewPassword("   ");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> userService.changePassword(created.getId(), dto)
        );

        assertThat(exception.getMessage())
            .contains("Validation of the dto for changing passwords failed");

        assertContainsErrorMessage(exception, "newPassword is blank");
    }

    @Test
    public void deleteCustomer_withValidId_deletesCustomerSuccessfully() {
        CustomerCreationDto dto = validCustomerDto(uniqueEmail("customer.delete"));
        UserDetailDto created = userService.createUser(dto);
        rememberCreatedUser(created);

        Long idToDelete = created.getId();

        userService.deleteUserById(idToDelete);

        assertAll(
            "Verify that customer is deleted",
            () -> assertThat(userRepository.findById(idToDelete)).isEmpty(),
            () -> assertThat(customerRepository.findById(idToDelete)).isEmpty()
        );
    }

    @Test
    public void deleteStaff_withValidId_deletesStaffSuccessfully() {
        StaffCreationDto dto = validStaffDto(uniqueEmail("staff.delete"));
        UserDetailDto created = userService.createUser(dto);
        rememberCreatedUser(created);

        Long idToDelete = created.getId();

        userService.deleteUserById(idToDelete);

        assertAll(
            "Verify that staff is deleted",
            () -> assertThat(userRepository.findById(idToDelete)).isEmpty(),
            () -> assertThat(staffRepository.findById(idToDelete)).isEmpty()
        );
    }

    @Test
    public void getUserById_withCustomerId_returnsCustomerSearchResponseDto() {
        Customer existingCustomer = customerRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseThrow();

        UserSearchResponseDto result =
            userService.getUserById(existingCustomer.getId());

        assertAll(
            "Verify that customer is returned as CustomerSearchResponseDto",

            () -> assertThat(result).isNotNull(),
            () -> assertThat(result).isInstanceOf(CustomerSearchResponseDto.class),

            () -> assertThat(result).extracting(UserSearchResponseDto::getUserName)
                .isEqualTo(existingCustomer.getUserName()),

            () -> assertThat(result).extracting(UserSearchResponseDto::getEmail)
                .isEqualTo(existingCustomer.getEmail()),

            () -> {
                CustomerSearchResponseDto customerDto =
                    (CustomerSearchResponseDto) result;

                assertThat(customerDto.getFirstName())
                    .isEqualTo(existingCustomer.getFirstName());

                assertThat(customerDto.getLastName())
                    .isEqualTo(existingCustomer.getLastName());

                assertThat(customerDto.getDateOfBirth())
                    .isEqualTo(existingCustomer.getDateOfBirth());
            }
        );
    }

    @Test
    public void getUserById_withStaffId_returnsStaffSearchResponseDto() {
        Staff existingStaff = staffRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseThrow();

        UserSearchResponseDto result =
            userService.getUserById(existingStaff.getId());

        assertAll(
            "Verify that staff is returned as StaffSearchResponseDto",

            () -> assertThat(result).isNotNull(),
            () -> assertThat(result).isInstanceOf(StaffSearchResponseDto.class),

            () -> assertThat(result).extracting(UserSearchResponseDto::getUserName)
                .isEqualTo(existingStaff.getUserName()),

            () -> assertThat(result).extracting(UserSearchResponseDto::getEmail)
                .isEqualTo(existingStaff.getEmail())
        );
    }

    @Test
    public void findApplicationUserByEmail_withExistingEmail_returnsUser() {
        CustomerCreationDto dto = validCustomerDto(uniqueEmail("customer.find"));
        UserDetailDto created = userService.createUser(dto);
        rememberCreatedUser(created);

        ApplicationUser result = userService.findApplicationUserByEmail(dto.getEmail());

        assertAll(
            "Verify that user can be found by email",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result).extracting(ApplicationUser::getId).isEqualTo(created.getId()),
            () -> assertThat(result).extracting(ApplicationUser::getEmail).isEqualTo(dto.getEmail()),
            () -> assertThat(result).extracting(ApplicationUser::getUserName).isEqualTo(dto.getUserName())
        );
    }

    @Test
    public void findApplicationUserByEmail_withUnknownEmail_throwsNotFoundException() {
        String unknownEmail = uniqueEmail("unknown.find");

        assertThrows(
            NotFoundException.class,
            () -> userService.findApplicationUserByEmail(unknownEmail)
        );
    }

    @Test
    public void loadUserByUsername_withExistingUser_returnsUserDetailsWithAuthorities() {
        StaffCreationDto dto = validStaffDto(uniqueEmail("staff.load"));
        UserDetailDto created = userService.createUser(dto);
        rememberCreatedUser(created);

        UserDetails result = userService.loadUserByUsername(dto.getEmail());

        assertAll(
            "Verify that Spring Security UserDetails are loaded correctly",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getUsername()).isEqualTo(dto.getEmail()),
            () -> assertThat(passwordEncoder.matches(dto.getPassword(), result.getPassword())).isTrue(),
            () -> assertThat(result.getAuthorities()).isNotEmpty()
        );
    }

    @Test
    public void loadUserByUsername_withUnknownEmail_throwsUsernameNotFoundException() {
        String unknownEmail = uniqueEmail("unknown.load");

        assertThrows(
            UsernameNotFoundException.class,
            () -> userService.loadUserByUsername(unknownEmail)
        );
    }

    @Test
    public void login_withValidCredentials_returnsJwtToken() {
        StaffCreationDto dto = validStaffDto(uniqueEmail("staff.login"));
        UserDetailDto created = userService.createUser(dto);
        rememberCreatedUser(created);

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail(dto.getEmail());
        loginDto.setPassword(dto.getPassword());

        String token = userService.login(loginDto);

        assertAll(
            "Verify that login returns a valid token",
            () -> assertThat(token).isNotNull(),
            () -> assertThat(token).isNotBlank()
        );
    }

    @Test
    public void login_withWrongPassword_throwsBadCredentialsException() {
        CustomerCreationDto dto = validCustomerDto(uniqueEmail("customer.login.wrong"));
        UserDetailDto created = userService.createUser(dto);
        rememberCreatedUser(created);

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail(dto.getEmail());
        loginDto.setPassword("DefinitelyWrongPassword123!");

        assertThrows(
            BadCredentialsException.class,
            () -> userService.login(loginDto)
        );
    }

    @Test
    public void login_withUnknownEmail_throwsUsernameNotFoundException() {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail(uniqueEmail("unknown.login"));
        loginDto.setPassword("Password123!");

        assertThrows(
            UsernameNotFoundException.class,
            () -> userService.login(loginDto)
        );
    }

    @Test
    public void deleteUserById_withUnknownId_throwsNotFoundException() {
        long unknownId = 99999999L;

        assertThrows(
            NotFoundException.class,
            () -> userService.deleteUserById(unknownId)
        );
    }

    @Test
    public void getUserById_withUnknownId_throwsNotFoundException() {
        long unknownId = 99999999L;

        assertThrows(
            NotFoundException.class,
            () -> userService.getUserById(unknownId)
        );
    }

    @Test
    public void searchCustomers_withFirstNameFilter_returnsMatchingCustomers() {
        CustomerCreationDto dto = validCustomerDto(uniqueEmail("customer.search"));
        dto.setFirstName("Searchlight");
        UserDetailDto created = userService.createUser(dto);
        rememberCreatedUser(created);

        CustomerSearchDto searchDto = new CustomerSearchDto();
        searchDto.setFirstName("Searchlight");

        List<UserSearchResponseDto> result = userService.searchCustomers(searchDto);

        assertAll(
            "Verify that the customer is found by first name filter",
            () -> assertThat(result).isNotEmpty(),
            () -> assertThat(result)
                .allSatisfy(r -> assertThat(r).isInstanceOf(CustomerSearchResponseDto.class)),
            () -> assertThat(result)
                .anySatisfy(r -> assertThat(r.getEmail()).isEqualTo(dto.getEmail()))
        );
    }

    @Test
    public void searchCustomers_withBlankFilters_doesNotThrowAndReturnsCustomers() {
        CustomerSearchDto searchDto = new CustomerSearchDto();
        searchDto.setEmail("");
        searchDto.setUserName("   ");
        searchDto.setFirstName(null);
        searchDto.setLastName(null);

        List<UserSearchResponseDto> result = assertDoesNotThrow(
            () -> userService.searchCustomers(searchDto)
        );

        assertThat(result).isNotNull();
    }
}