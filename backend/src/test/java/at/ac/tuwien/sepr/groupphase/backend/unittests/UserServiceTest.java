package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.CustomerCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.StaffCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.CustomerUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.StaffUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.StaffRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

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
    private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    @Rollback
    public void createCustomer_withValidDto_returnsSavedCustomerWithId() {

        CustomerCreationDto dto = new CustomerCreationDto();

        dto.setUserName("max_customer");
        dto.setPassword("Password123!");
        dto.setEmail("max.customer@test.at");
        dto.setFirstName("Max");
        dto.setLastName("Mustermann");
        dto.setDateOfBirth(LocalDate.of(1998, 5, 10));

        UserDetailDto created = userService.createUser(dto);

        ApplicationUser savedApplicationUser = userRepository.findUserByEmail(dto.getEmail()).orElseThrow();
        Customer savedCustomer = customerRepository.findById(created.getId()).orElseThrow();

        assertAll(
            "Verify that the customer is saved correctly and assigned an ID",

            //Dto check
            () -> assertThat(created).isNotNull(),
            () -> assertThat(created.getId()).isNotNull(),
            () -> assertThat(created.getEmail()).isEqualTo(dto.getEmail()),
            () -> assertThat(created.getUserName()).isEqualTo(dto.getUserName()),

            //UserRepositoy checks
            () -> assertThat(savedApplicationUser).isInstanceOf(Customer.class),
            () -> assertThat(savedApplicationUser.getId()).isNotNull(),
            () -> assertThat(savedApplicationUser.getEmail()).isEqualTo(dto.getEmail()),
            () -> assertThat(savedApplicationUser.getUserName()).isEqualTo(dto.getUserName()),

            () -> assertThat(passwordEncoder.matches(
                dto.getPassword(),
                savedApplicationUser.getPassword()
            )).isTrue(),

            //CustomerRepository checks
            () -> assertThat(savedCustomer.getFirstName()).isEqualTo(dto.getFirstName()),
            () -> assertThat(savedCustomer.getLastName()).isEqualTo(dto.getLastName()),
            () -> assertThat(savedCustomer.getDateOfBirth()).isEqualTo(dto.getDateOfBirth()),
            () -> assertThat(savedCustomer.getProfiles()).isNotNull(),
            () -> assertThat(savedCustomer.getProfiles()).isEmpty()
        );
    }


    @Test
    @Transactional
    @Rollback
    public void createStaff_withValidDto_returnsSavedStaffWithId() {

        StaffCreationDto dto = new StaffCreationDto();

        dto.setUserName("staff_user");
        dto.setPassword("Password123!");
        dto.setEmail("staff@test.at");

        UserDetailDto created = userService.createUser(dto);

        ApplicationUser saved = userRepository
            .findUserByEmail(dto.getEmail())
            .orElseThrow();

        Staff savedStaff = staffRepository
            .findById(created.getId())
            .orElseThrow();

        assertAll(
            "Verify that the staff user is saved correctly and assigned an ID",

            //Dto check
            () -> assertThat(created).isNotNull(),
            () -> assertThat(created.getId()).isNotNull(),
            () -> assertThat(created.getEmail()).isEqualTo(dto.getEmail()),
            () -> assertThat(created.getUserName()).isEqualTo(dto.getUserName()),

            //UserRepositoy check
            () -> assertThat(saved).isInstanceOf(Staff.class),
            () -> assertThat(saved.getId()).isNotNull(),
            () -> assertThat(saved.getEmail()).isEqualTo(dto.getEmail()),
            () -> assertThat(saved.getUserName()).isEqualTo(dto.getUserName()),

            //StaffRepository check
            () -> assertThat(savedStaff.getId()).isEqualTo(created.getId()),
            () -> assertThat(savedStaff.getEmail()).isEqualTo(dto.getEmail()),
            () -> assertThat(savedStaff.getUserName()).isEqualTo(dto.getUserName())
        );
    }


    @Test
    @Transactional
    @Rollback
    public void updateCustomer_withValidDto_updatesCustomerCorrectly() {

        Customer existingCustomer = customerRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseThrow();

        CustomerUpdateDto dto = new CustomerUpdateDto();

        dto.setUserName("updated_customer");
        dto.setPassword("NewPassword123!");
        dto.setEmail("updated.customer@test.at");
        dto.setFirstName("UpdatedFirst");
        dto.setLastName("UpdatedLast");
        dto.setDateOfBirth(LocalDate.of(2000, 1, 1));

        UserDetailDto updated = userService.updateUser(existingCustomer.getId(), dto);

        ApplicationUser savedApplicationUser = userRepository
            .findById(existingCustomer.getId())
            .orElseThrow();

        Customer savedCustomer = customerRepository
            .findById(existingCustomer.getId())
            .orElseThrow();

        assertAll(
            "Verify that the customer was updated correctly",

            //Returned DTO
            () -> assertThat(updated).isNotNull(),
            () -> assertThat(updated.getId()).isEqualTo(existingCustomer.getId()),
            () -> assertThat(updated.getUserName()).isEqualTo(dto.getUserName()),
            () -> assertThat(updated.getEmail()).isEqualTo(dto.getEmail()),

            //User repository
            () -> assertThat(savedApplicationUser.getUserName()).isEqualTo(dto.getUserName()),
            () -> assertThat(savedApplicationUser.getEmail()).isEqualTo(dto.getEmail()),

            () -> assertThat(passwordEncoder.matches(
                dto.getPassword(),
                savedApplicationUser.getPassword()
            )).isTrue(),

            //Customer specific fields
            () -> assertThat(savedCustomer.getFirstName()).isEqualTo(dto.getFirstName()),
            () -> assertThat(savedCustomer.getLastName()).isEqualTo(dto.getLastName()),
            () -> assertThat(savedCustomer.getDateOfBirth()).isEqualTo(dto.getDateOfBirth())
        );
    }


    @Test
    @Transactional
    @Rollback
    public void updateStaff_withValidDto_updatesStaffCorrectly() {

        Staff existingStaff = staffRepository
            .findAll()
            .stream()
            .findFirst()
            .orElseThrow();

        StaffUpdateDto dto = new StaffUpdateDto();

        dto.setUserName("updated_staff");
        dto.setPassword("UpdatedPassword123!");
        dto.setEmail("updated.staff@test.at");

        UserDetailDto updated = userService.updateUser(existingStaff.getId(), dto);

        ApplicationUser savedApplicationUser = userRepository
            .findById(existingStaff.getId())
            .orElseThrow();

        Staff savedStaff = staffRepository
            .findById(existingStaff.getId())
            .orElseThrow();

        assertAll(
            "Verify that the staff user was updated correctly",

            //Returned DTO
            () -> assertThat(updated).isNotNull(),
            () -> assertThat(updated.getId()).isEqualTo(existingStaff.getId()),
            () -> assertThat(updated.getUserName()).isEqualTo(dto.getUserName()),
            () -> assertThat(updated.getEmail()).isEqualTo(dto.getEmail()),

            //Repository state
            () -> assertThat(savedStaff.getUserName()).isEqualTo(dto.getUserName()),
            () -> assertThat(savedStaff.getEmail()).isEqualTo(dto.getEmail()),

            //Password encoded correctly
            () -> assertThat(passwordEncoder.matches(
                dto.getPassword(),
                savedApplicationUser.getPassword()
            )).isTrue()
        );
    }
}
