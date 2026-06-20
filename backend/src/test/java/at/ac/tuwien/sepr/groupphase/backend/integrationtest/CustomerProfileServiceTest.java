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
import at.ac.tuwien.sepr.groupphase.backend.repository.user.StaffRepository;
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

    @Autowired
    private StaffRepository staffRepository;

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

    @Test
    public void createCustomerProfile_withValidDto_returnsSavedProfileWithId() {
        Customer savedCustomer = createTestCustomer("create_valid");

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

        when(currentUserService.getUserId())
            .thenReturn(99999L);

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
                .containsIgnoringCase("cannot be null")
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

        when(currentUserService.getUserId())
            .thenReturn(customer.getId());

        customerProfileService.deleteCustomerProfile(profile.getId());

        assertThat(customerProfileRepository.existsById(profile.getId())).isFalse();
    }

    @Test
    public void deleteCustomerProfile_withUnknownProfile_throwsNotFoundException() {
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
    public void updateCustomerProfile_withValidDto_updatesOnlyProvidedFields() {
        Customer customer = createTestCustomer("update_profile");
        CustomerProfile profile = createTestProfile(customer, "Old Profile Name", SkillLevel.BEGINNER);

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
    public void updateCustomerProfile_withUnknownProfile_throwsNotFoundException() {
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
    public void updateCustomerProfile_withEmptyDto_throwsValidationException() {
        Customer customer = createTestCustomer("empty_update");
        CustomerProfile profile = createTestProfile(customer, "Empty Update Profile", SkillLevel.BEGINNER);

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

        CustomerProfileUpdateDto dto = new CustomerProfileUpdateDto();
        dto.setProfileName("");

        ValidationException exception = assertThrows(ValidationException.class, () ->
            customerProfileService.updateCustomerProfile(profile.getId(), dto)
        );

        assertThat(exception).isNotNull();
    }
}