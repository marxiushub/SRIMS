package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Permission;
import at.ac.tuwien.sepr.groupphase.backend.entity.Role;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.springframework.security.access.AccessDeniedException;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.CurrentUserService;
import at.ac.tuwien.sepr.groupphase.backend.service.CustomerProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

//TODO: rework test to use datagenerators and integrationtestbase
@ActiveProfiles({"test"})
@SpringBootTest
public class CustomerProfileServiceTest {

    @Autowired
    private CustomerProfileService customerProfileService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @MockitoBean
    private CurrentUserService currentUserService;

    @AfterEach
    public void cleanup() {
        customerProfileRepository.deleteAll();
        customerRepository.deleteAll();
    }

    private Customer createTestCustomer(String suffix) {
        Customer customer = new Customer(
            "profile_user_" + suffix,
            "hashedPassword",
            "profile.user." + suffix + "@example.com",
            Set.<Role>of(),
            Set.<Permission>of(),
            "Profile",
            "Tester",
            LocalDate.of(1999, 1, 1)
        );

        return customerRepository.save(customer);
    }

    private CustomerProfile createTestProfile(Customer customer, String profileName, SkillLevel skillLevel) {
        CustomerProfile profile = new CustomerProfile(
            profileName,
            175,
            70,
            42,
            skillLevel,
            customer
        );

        return customerProfileRepository.save(profile);
    }

    // Sets up the mock so the service believes "customer" is the currently authenticated, non-staff user.
    private void actingAsCustomer(Customer customer) {
        when(currentUserService.getUserId()).thenReturn(customer.getId());
        when(currentUserService.hasAuthority("STAFF")).thenReturn(false);
    }

    private void actingAsStaff(Long staffId) {
        when(currentUserService.getUserId()).thenReturn(staffId);
        when(currentUserService.hasAuthority("STAFF")).thenReturn(true);
    }

    @Test
    public void createCustomerProfile_withValidDto_returnsSavedProfileWithId() {
        Customer savedCustomer = createTestCustomer("create_valid");
        actingAsCustomer(savedCustomer);

        when(currentUserService.getUserId())
            .thenReturn(savedCustomer.getId());

        CustomerProfileCreationDto dto = new CustomerProfileCreationDto();
        dto.setProfileName("Test Profile");
        dto.setHeight(175);
        dto.setWeight(70);
        dto.setShoeSize(42);
        dto.setSkillLevel(SkillLevel.BEGINNER);

        CustomerProfileDetailDto created = customerProfileService.createCustomerProfile(dto);

        CustomerProfile profile = customerProfileRepository.findById(created.getId()).orElseThrow();

        assertAll(
            "Verify that the customer profile is saved correctly and mapped to DTO",
            () -> assertThat(created).isNotNull(),
            () -> assertThat(created.getId()).isNotNull(),
            () -> assertThat(created.getCustomerId()).isEqualTo(savedCustomer.getId()),
            () -> assertThat(created.getProfileName()).isEqualTo(dto.getProfileName()),
            () -> assertThat(created.getHeight()).isEqualTo(dto.getHeight()),
            () -> assertThat(created.getWeight()).isEqualTo(dto.getWeight()),
            () -> assertThat(created.getShoeSize()).isEqualTo(dto.getShoeSize()),
            () -> assertThat(created.getSkillLevel()).isEqualTo(dto.getSkillLevel()),
            () -> assertThat(profile.getCustomer().getId()).isEqualTo(savedCustomer.getId()),
            () -> assertThat(profile.getProfileName()).isEqualTo(dto.getProfileName())
        );
    }

    @Test
    public void createCustomerProfile_withUnknownCustomerId_throwsNotFoundException() {
        // No customer with this id exists in the DB; service reads the id from the security context.
        when(currentUserService.getUserId()).thenReturn(99999L);

        CustomerProfileCreationDto dto = new CustomerProfileCreationDto();
        dto.setProfileName("Unknown Customer Profile");
        dto.setHeight(175);
        dto.setWeight(70);
        dto.setShoeSize(42);
        dto.setSkillLevel(SkillLevel.BEGINNER);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            customerProfileService.createCustomerProfile(dto)
        );

        assertAll(
            "Verify that creating a profile for an unknown customer fails",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage()).containsIgnoringCase("not found")
        );
    }

    @Test
    public void getCustomerProfiles_withExistingCustomer_returnsProfiles() {
        Customer customer = createTestCustomer("list_profiles");
        actingAsCustomer(customer);

        when(currentUserService.getUserId())
            .thenReturn(customer.getId());

        createTestProfile(customer, "First Test Profile", SkillLevel.BEGINNER);
        createTestProfile(customer, "Second Test Profile", SkillLevel.ADVANCED);

        List<CustomerProfileDetailDto> result = customerProfileService.getCustomerProfiles();

        assertAll(
            "Verify that all profiles for the customer are returned",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result).hasSize(2),
            () -> assertThat(result)
                .extracting(CustomerProfileDetailDto::getProfileName)
                .containsExactlyInAnyOrder("First Test Profile", "Second Test Profile"),
            () -> assertThat(result)
                .allMatch(profile -> profile.getCustomerId().equals(customer.getId()))
        );
    }

    @Test
    public void getCustomerProfiles_withUnknownCustomer_throwsNotFoundException() {

        when(currentUserService.getUserId())
            .thenReturn(99999L);

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> customerProfileService.getCustomerProfiles()
        );

        assertAll(
            "Verify that getting profiles for an unknown customer fails",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage())
                .containsIgnoringCase("not found")
        );
    }

    @Test
    public void getCustomerProfiles_withExistingCustomerId_returnsProfiles() {
        Customer customer = createTestCustomer("staff_list_profiles");

        createTestProfile(customer, "First Test Profile", SkillLevel.BEGINNER);
        createTestProfile(customer, "Second Test Profile", SkillLevel.ADVANCED);

        List<CustomerProfileDetailDto> result =
            customerProfileService.getCustomerProfiles(customer.getId());

        assertAll(
            "Verify that all profiles for the given customer are returned",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result).hasSize(2),
            () -> assertThat(result)
                .extracting(CustomerProfileDetailDto::getProfileName)
                .containsExactlyInAnyOrder(
                    "First Test Profile",
                    "Second Test Profile"
                ),
            () -> assertThat(result)
                .allMatch(profile ->
                    profile.getCustomerId().equals(customer.getId()))
        );
    }

    @Test
    public void getCustomerProfiles_withUnknownCustomerId_throwsNotFoundException() {

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> customerProfileService.getCustomerProfiles(99999L)
        );

        assertAll(
            "Verify that getting profiles for an unknown customer fails",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage())
                .containsIgnoringCase("not found")
        );
    }

    @Test
    public void getCustomerProfiles_withNullCustomerId_throwsIllegalArgumentException() {

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> customerProfileService.getCustomerProfiles(null)
        );

        assertAll(
            "Verify that a null customer id is rejected",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage())
                .containsIgnoringCase("Customer profile ID is null")
        );
    }

    @Test
    public void getCustomerProfile_withOwnProfile_returnsProfile() {
        Customer customer = createTestCustomer("own_profile");

        CustomerProfile profile = createTestProfile(
            customer,
            "My Profile",
            SkillLevel.BEGINNER
        );

        when(currentUserService.getUserId())
            .thenReturn(customer.getId());

        when(currentUserService.hasAuthority("STAFF"))
            .thenReturn(false);

        CustomerProfileDetailDto result =
            customerProfileService.getCustomerProfileById(profile.getId());

        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(profile.getId()),
            () -> assertThat(result.getProfileName()).isEqualTo("My Profile"),
            () -> assertThat(result.getCustomerId()).isEqualTo(customer.getId())
        );
    }

    @Test
    public void getCustomerProfile_asStaff_returnsProfile() {
        Customer customer = createTestCustomer("staff_access");

        CustomerProfile profile = createTestProfile(
            customer,
            "Customer Profile",
            SkillLevel.ADVANCED
        );

        when(currentUserService.getUserId())
            .thenReturn(999L);

        when(currentUserService.hasAuthority("STAFF"))
            .thenReturn(true);

        CustomerProfileDetailDto result =
            customerProfileService.getCustomerProfileById(profile.getId());

        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(profile.getId()),
            () -> assertThat(result.getCustomerId()).isEqualTo(customer.getId())
        );
    }

    @Test
    public void getCustomerProfile_withForeignProfile_throwsAccessDeniedException() {
        Customer owner = createTestCustomer("owner");
        Customer otherCustomer = createTestCustomer("other");

        CustomerProfile profile = createTestProfile(
            owner,
            "Protected Profile",
            SkillLevel.BEGINNER
        );

        when(currentUserService.getUserId())
            .thenReturn(otherCustomer.getId());

        when(currentUserService.hasAuthority("STAFF"))
            .thenReturn(false);

        AccessDeniedException exception = assertThrows(
            AccessDeniedException.class,
            () -> customerProfileService.getCustomerProfileById(profile.getId())
        );

        assertThat(exception.getMessage())
            .containsIgnoringCase("permission");
    }

    @Test
    public void getCustomerProfile_withUnknownProfile_throwsNotFoundException() {

        when(currentUserService.getUserId())
            .thenReturn(1L);

        when(currentUserService.hasAuthority("STAFF"))
            .thenReturn(false);

        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> customerProfileService.getCustomerProfileById(99999L)
        );

        assertThat(exception.getMessage())
            .containsIgnoringCase("not found");
    }

    @Test
    public void deleteCustomerProfile_withExistingProfile_deletesProfile() {
        Customer customer = createTestCustomer("delete_profile");
        CustomerProfile profile = createTestProfile(customer, "Profile To Delete", SkillLevel.BEGINNER);
        actingAsCustomer(customer);

        when(currentUserService.getUserId())
            .thenReturn(customer.getId());

        customerProfileService.deleteCustomerProfile(profile.getId());

        assertThat(customerProfileRepository.existsById(profile.getId())).isFalse();
    }

    @Test
    public void deleteCustomerProfile_withUnknownProfile_throwsNotFoundException() {
        Customer customer = createTestCustomer("delete_unknown");
        actingAsCustomer(customer);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            customerProfileService.deleteCustomerProfile(99999L)
        );

        assertAll(
            "Verify that deleting an unknown customer profile fails",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage()).containsIgnoringCase("not found")
        );
    }

    @Test
    public void deleteCustomerProfile_belongingToAnotherCustomer_throwsAccessDenied() {
        Customer owner = createTestCustomer("delete_owner");
        Customer attacker = createTestCustomer("delete_attacker");
        CustomerProfile profile = createTestProfile(owner, "Owner Profile", SkillLevel.BEGINNER);

        actingAsCustomer(attacker);

        assertThrows(AccessDeniedException.class, () ->
            customerProfileService.deleteCustomerProfile(profile.getId())
        );
    }

    @Test
    public void updateCustomerProfile_withValidDto_updatesOnlyProvidedFields() {
        Customer customer = createTestCustomer("update_profile");
        CustomerProfile profile = createTestProfile(customer, "Old Profile Name", SkillLevel.BEGINNER);
        actingAsCustomer(customer);

        when(currentUserService.getUserId())
            .thenReturn(customer.getId());

        CustomerProfileUpdateDto dto = new CustomerProfileUpdateDto();
        dto.setProfileName("Updated Profile Name");
        dto.setHeight(180.0);
        dto.setSkillLevel(SkillLevel.ADVANCED);

        CustomerProfileDetailDto result = customerProfileService.updateCustomerProfile(profile.getId(), dto);

        CustomerProfile updatedProfile = customerProfileRepository.findById(profile.getId()).orElseThrow();

        assertAll(
            "Verify that only provided fields are updated",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(profile.getId()),
            () -> assertThat(result.getProfileName()).isEqualTo("Updated Profile Name"),
            () -> assertThat(result.getHeight()).isEqualTo(180.0),
            () -> assertThat(result.getWeight()).isEqualTo(70),
            () -> assertThat(result.getShoeSize()).isEqualTo(42),
            () -> assertThat(result.getSkillLevel()).isEqualTo(SkillLevel.ADVANCED),
            () -> assertThat(updatedProfile.getProfileName()).isEqualTo("Updated Profile Name"),
            () -> assertThat(updatedProfile.getHeight()).isEqualTo(180.0),
            () -> assertThat(updatedProfile.getWeight()).isEqualTo(70),
            () -> assertThat(updatedProfile.getShoeSize()).isEqualTo(42),
            () -> assertThat(updatedProfile.getSkillLevel()).isEqualTo(SkillLevel.ADVANCED)
        );
    }

    @Test
    public void updateCustomerProfile_withOnlyWeightAndShoeSize_updatesOnlyThoseFields() {
        Customer customer = createTestCustomer("update_weight_shoe");
        CustomerProfile profile = createTestProfile(customer, "Weight Shoe Test", SkillLevel.BEGINNER);
        actingAsCustomer(customer);

        CustomerProfileUpdateDto dto = new CustomerProfileUpdateDto();
        dto.setWeight(85.0);
        dto.setShoeSize(44.0);

        CustomerProfileDetailDto result = customerProfileService.updateCustomerProfile(profile.getId(), dto);

        assertAll(
            "Verify that weight and shoeSize are updated, other fields unchanged",
            () -> assertThat(result.getWeight()).isEqualTo(85.0),
            () -> assertThat(result.getShoeSize()).isEqualTo(44.0),
            () -> assertThat(result.getProfileName()).isEqualTo("Weight Shoe Test"),
            () -> assertThat(result.getHeight()).isEqualTo(175.0),
            () -> assertThat(result.getSkillLevel()).isEqualTo(SkillLevel.BEGINNER)
        );
    }

    @Test
    public void updateCustomerProfile_withUnknownProfile_throwsNotFoundException() {
        Customer customer = createTestCustomer("update_unknown");
        actingAsCustomer(customer);

        CustomerProfileUpdateDto dto = new CustomerProfileUpdateDto();
        dto.setProfileName("Updated Profile Name");

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            customerProfileService.updateCustomerProfile(99999L, dto)
        );

        assertAll(
            "Verify that updating an unknown customer profile fails",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage()).containsIgnoringCase("not found")
        );
    }

    @Test
    public void updateCustomerProfile_belongingToAnotherCustomer_throwsAccessDenied() {
        Customer owner = createTestCustomer("update_owner");
        Customer attacker = createTestCustomer("update_attacker");
        CustomerProfile profile = createTestProfile(owner, "Owner Profile", SkillLevel.BEGINNER);

        actingAsCustomer(attacker);

        CustomerProfileUpdateDto dto = new CustomerProfileUpdateDto();
        dto.setProfileName("Hacked Name");

        assertThrows(AccessDeniedException.class, () ->
            customerProfileService.updateCustomerProfile(profile.getId(), dto)
        );
    }

    @Test
    public void updateCustomerProfile_withEmptyDto_throwsValidationException() {
        Customer customer = createTestCustomer("empty_update");
        CustomerProfile profile = createTestProfile(customer, "Empty Update Profile", SkillLevel.BEGINNER);
        actingAsCustomer(customer);

        CustomerProfileUpdateDto dto = new CustomerProfileUpdateDto();

        ValidationException exception = assertThrows(ValidationException.class, () ->
            customerProfileService.updateCustomerProfile(profile.getId(), dto)
        );

        assertThat(exception).isNotNull();
    }

    @Test
    public void updateCustomerProfile_withBlankProfileName_throwsValidationException() {
        Customer customer = createTestCustomer("blank_profile_name");
        CustomerProfile profile = createTestProfile(customer, "Blank Profile Name Test", SkillLevel.BEGINNER);
        actingAsCustomer(customer);

        CustomerProfileUpdateDto dto = new CustomerProfileUpdateDto();
        dto.setProfileName("");

        ValidationException exception = assertThrows(ValidationException.class, () ->
            customerProfileService.updateCustomerProfile(profile.getId(), dto)
        );

        assertThat(exception).isNotNull();
    }

    @Test
    public void getCustomerProfileById_ownProfile_returnsProfile() {
        Customer customer = createTestCustomer("get_by_id");
        CustomerProfile profile = createTestProfile(customer, "Profile By Id", SkillLevel.BEGINNER);
        actingAsCustomer(customer);

        CustomerProfileDetailDto result = customerProfileService.getCustomerProfileById(profile.getId());

        assertAll(
            "Verify that a customer profile can be retrieved by ID",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(profile.getId()),
            () -> assertThat(result.getCustomerId()).isEqualTo(customer.getId()),
            () -> assertThat(result.getProfileName()).isEqualTo("Profile By Id"),
            () -> assertThat(result.getHeight()).isEqualTo(175),
            () -> assertThat(result.getWeight()).isEqualTo(70),
            () -> assertThat(result.getShoeSize()).isEqualTo(42),
            () -> assertThat(result.getSkillLevel()).isEqualTo(SkillLevel.BEGINNER)
        );
    }

    @Test
    public void getCustomerProfileById_asStaff_returnsProfileEvenIfNotOwner() {
        Customer customer = createTestCustomer("get_by_id_staff_access");
        CustomerProfile profile = createTestProfile(customer, "Profile Seen By Staff", SkillLevel.BEGINNER);
        actingAsStaff(999L);

        CustomerProfileDetailDto result = customerProfileService.getCustomerProfileById(profile.getId());

        assertThat(result.getProfileName()).isEqualTo("Profile Seen By Staff");
    }

    @Test
    public void getCustomerProfileById_belongingToAnotherCustomer_throwsAccessDenied() {
        Customer owner = createTestCustomer("get_by_id_owner");
        Customer attacker = createTestCustomer("get_by_id_attacker");
        CustomerProfile profile = createTestProfile(owner, "Owner Profile", SkillLevel.BEGINNER);

        actingAsCustomer(attacker);

        assertThrows(AccessDeniedException.class, () ->
            customerProfileService.getCustomerProfileById(profile.getId())
        );
    }

    @Test
    public void getCustomerProfileById_withUnknownProfile_throwsNotFoundException() {
        actingAsCustomer(createTestCustomer("get_by_id_unknown"));

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            customerProfileService.getCustomerProfileById(99999L)
        );

        assertAll(
            "Verify that getting an unknown customer profile fails",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage()).containsIgnoringCase("not found")
        );
    }

    //Validator Tests:
    @Test
    public void createCustomerProfile_withNullDto_throwsIllegalArgumentException() {
        Customer customer = createTestCustomer("null_dto");
        actingAsCustomer(customer);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> customerProfileService.createCustomerProfile(null)
        );

        assertThat(exception.getMessage()).contains("null");
    }

    @Test
    public void createCustomerProfile_withBlankProfileName_throwsValidationException() {
        Customer customer = createTestCustomer("blank_name");
        actingAsCustomer(customer);

        CustomerProfileCreationDto dto = new CustomerProfileCreationDto();
        dto.setProfileName("   ");
        dto.setHeight(175);
        dto.setWeight(70);
        dto.setShoeSize(42);
        dto.setSkillLevel(SkillLevel.BEGINNER);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> customerProfileService.createCustomerProfile(dto)
        );

        assertThat(exception.getMessage()).containsIgnoringCase("creation failed");
    }

    @Test
    public void createCustomerProfile_withTooLongProfileName_throwsValidationException() {
        Customer customer = createTestCustomer("long_name");
        actingAsCustomer(customer);

        CustomerProfileCreationDto dto = new CustomerProfileCreationDto();
        dto.setProfileName("a".repeat(101)); // > 100 chars
        dto.setHeight(175);
        dto.setWeight(70);
        dto.setShoeSize(42);
        dto.setSkillLevel(SkillLevel.BEGINNER);

        assertThrows(
            ValidationException.class,
            () -> customerProfileService.createCustomerProfile(dto)
        );
    }

    @Test
    public void createCustomerProfile_withDuplicateProfileName_throwsValidationException() {
        Customer customer = createTestCustomer("duplicate_name");
        actingAsCustomer(customer);

        createTestProfile(customer, "Duplicate", SkillLevel.BEGINNER);

        CustomerProfileCreationDto dto = new CustomerProfileCreationDto();
        dto.setProfileName("Duplicate"); // same name + same customer
        dto.setHeight(175);
        dto.setWeight(70);
        dto.setShoeSize(42);
        dto.setSkillLevel(SkillLevel.BEGINNER);

        assertThrows(
            ValidationException.class,
            () -> customerProfileService.createCustomerProfile(dto)
        );
    }

    @Test
    public void updateCustomerProfile_withNullDto_throwsIllegalArgumentException() {
        Customer customer = createTestCustomer("null_update");
        CustomerProfile profile = createTestProfile(customer, "Name", SkillLevel.BEGINNER);
        actingAsCustomer(customer);

        assertThrows(
            IllegalArgumentException.class,
            () -> customerProfileService.updateCustomerProfile(profile.getId(), null)
        );
    }

    @Test
    public void updateCustomerProfile_withOnlyWhitespaceFields_throwsValidationException() {
        Customer customer = createTestCustomer("whitespace_update");
        CustomerProfile profile = createTestProfile(customer, "Valid", SkillLevel.BEGINNER);
        actingAsCustomer(customer);

        CustomerProfileUpdateDto dto = new CustomerProfileUpdateDto();
        dto.setProfileName("   "); // invalid after trim

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> customerProfileService.updateCustomerProfile(profile.getId(), dto)
        );

        assertThat(exception.getErrors())
            .anyMatch(msg -> msg.contains("blank") || msg.contains("Profile name"));
    }

    @Test
    public void updateCustomerProfile_withAllNullFields_throwsValidationException() {
        Customer customer = createTestCustomer("all_null");
        CustomerProfile profile = createTestProfile(customer, "Valid", SkillLevel.BEGINNER);
        actingAsCustomer(customer);

        CustomerProfileUpdateDto dto = new CustomerProfileUpdateDto();

        assertThrows(
            ValidationException.class,
            () -> customerProfileService.updateCustomerProfile(profile.getId(), dto)
        );
    }
}