package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;

import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.CustomerProfileService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles({"test", "generateData"})
@SpringBootTest
public class CustomerProfileServiceTest {

    @Autowired
    private CustomerProfileService customerProfileService;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @Transactional
    @Rollback
    public void createCustomerProfile_withValidDto_returnSavedProfileWithId() {
        Customer customer = new Customer(
            "profile_test_user",
            "hashedPassword",
            "profile.test@example.com",
            "Profile",
            "Tester",
            LocalDate.of(1999,1,1)
        );
        Customer savedCustomer = customerRepository.save(customer);

        CustomerProfileCreationDto dto = new CustomerProfileCreationDto();
        dto.setCustomerId(savedCustomer.getId());
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
    @Transactional
    @Rollback
    public void createCustomerProfile_withUnknownCustomerId_throwsNotFoundException() {
        CustomerProfileCreationDto dto = new CustomerProfileCreationDto();
        dto.setCustomerId(99999L);
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
    @Transactional
    @Rollback
    public void getCustomerProfiles_withExistingCustomer_returnsProfiles() {
        Customer customer = new Customer(
            "list_profile_user",
            "hashedPassword",
            "list.profile@example.com",
            "List",
            "Tester",
            LocalDate.of(1999, 1, 1)
        );

        Customer savedCustomer = customerRepository.save(customer);

        CustomerProfile firstProfile = new CustomerProfile(
            "First Test Profile",
            175,
            70,
            42,
            SkillLevel.BEGINNER,
            savedCustomer
        );

        CustomerProfile secondProfile = new CustomerProfile(
            "Second Test Profile",
            180,
            80,
            44,
            SkillLevel.ADVANCED,
            savedCustomer
        );

        customerProfileRepository.save(firstProfile);
        customerProfileRepository.save(secondProfile);

        List<CustomerProfileDetailDto> result = customerProfileService.getCustomerProfiles(savedCustomer.getId());

        assertAll(
            "Verify that all profiles for the customer are returned",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result).hasSize(2),
            () -> assertThat(result)
                .extracting(CustomerProfileDetailDto::getProfileName)
                .containsExactlyInAnyOrder("First Test Profile", "Second Test Profile"),
            () -> assertThat(result)
                .allMatch(profile -> profile.getCustomerId().equals(savedCustomer.getId()))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void getCustomerProfiles_withUnknownCustomer_throwsNotFoundException() {
        Long unknownCustomerId = 99999L;

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            customerProfileService.getCustomerProfiles(unknownCustomerId)
        );

        assertAll(
            "Verify that getting profiles for an unknown customer fails",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage()).containsIgnoringCase("not found")
        );
    }

    @Test
    @Transactional
    @Rollback
    public void deleteCustomerProfile_withExistingProfile_deletesProfile() {
        Customer customer = new Customer(
            "delete_profile_user",
            "hashedPassword",
            "delete.profile@example.com",
            "Delete",
            "Tester",
            LocalDate.of(1989, 2, 2)
        );

        Customer savedCustomer = customerRepository.save(customer);

        CustomerProfile profile = new CustomerProfile(
            "Profile To Delete",
            175,
            70,
            42,
            SkillLevel.BEGINNER,
            savedCustomer
        );

        CustomerProfile savedProfile = customerProfileRepository.save(profile);

        customerProfileService.deleteCustomerProfile(savedProfile.getId());

        assertThat(customerProfileRepository.existsById(savedProfile.getId())).isFalse();
    }

    @Test
    @Transactional
    @Rollback
    public void deleteCustomerProfile_withUnknownProfile_throwsNotFoundException() {
        Long unknownProfileId = 99999L;

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            customerProfileService.deleteCustomerProfile(unknownProfileId)
        );

        assertAll(
            "Verify that deleting an unknown customer profile fails",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage()).containsIgnoringCase("not found")
        );
    }
}
