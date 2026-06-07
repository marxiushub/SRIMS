package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Permission;
import at.ac.tuwien.sepr.groupphase.backend.entity.Role;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.UserType;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import at.ac.tuwien.sepr.groupphase.backend.repository.PermissionRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.StaffRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    @Order(1)
    CommandLineRunner initUsers(
        UserRepository userRepository,
        PasswordEncoder encoder,
        CustomerRepository customerRepository,
        CustomerProfileRepository profileRepository,
        RoleRepository roleRepository,
        PermissionRepository permissionRepository) {
        return args -> {

            //PERMISSIONS
            //Equipment
            Permission createEquipment = permissionRepository.findByName("EQUIPMENT_CREATE")
                .orElseGet(() -> permissionRepository.save(new Permission("EQUIPMENT_CREATE")));

            Permission readEquipment = permissionRepository.findByName("EQUIPMENT_READ")
                .orElseGet(() -> permissionRepository.save(new Permission("EQUIPMENT_READ")));

            Permission updateEquipment = permissionRepository.findByName("EQUIPMENT_UPDATE")
                .orElseGet(() -> permissionRepository.save(new Permission("EQUIPMENT_UPDATE")));

            Permission deleteEquipment = permissionRepository.findByName("EQUIPMENT_DELETE")
                .orElseGet(() -> permissionRepository.save(new Permission("EQUIPMENT_DELETE")));

            //___________________________________________________________________________________________

            //Reservation
            Permission createReservation = permissionRepository.findByName("RESERVATION_CREATE")
                .orElseGet(() -> permissionRepository.save(new Permission("RESERVATION_CREATE")));

            Permission readReservation = permissionRepository.findByName("RESERVATION_READ")
                .orElseGet(() -> permissionRepository.save(new Permission("RESERVATION_READ")));

            Permission updateReservation = permissionRepository.findByName("RESERVATION_UPDATE")
                .orElseGet(() -> permissionRepository.save(new Permission("RESERVATION_UPDATE")));

            Permission deleteReservation = permissionRepository.findByName("RESERVATION_DELETE")
                .orElseGet(() -> permissionRepository.save(new Permission("RESERVATION_DELETE")));

            //___________________________________________________________________________________________

            //CustomerProfile
            Permission createCustomerProfile = permissionRepository.findByName("CUSTOMERPROFILE_CREATE")
                .orElseGet(() -> permissionRepository.save(new Permission("CUSTOMERPROFILE_CREATE")));

            Permission readCustomerProfile = permissionRepository.findByName("CUSTOMERPROFILE_READ")
                .orElseGet(() -> permissionRepository.save(new Permission("CUSTOMERPROFILE_READ")));

            Permission updateCustomerProfile = permissionRepository.findByName("CUSTOMERPROFILE_UPDATE")
                .orElseGet(() -> permissionRepository.save(new Permission("CUSTOMERPROFILE_UPDATE")));

            Permission deleteCustomerProfile = permissionRepository.findByName("CUSTOMERPROFILE_DELETE")
                .orElseGet(() -> permissionRepository.save(new Permission("CUSTOMERPROFILE_DELETE")));

            //___________________________________________________________________________________________

            //Customer
            Permission readCustomer = permissionRepository.findByName("CUSTOMER_READ")
                .orElseGet(() -> permissionRepository.save(new Permission("CUSTOMER_READ")));

            Permission updateCustomer = permissionRepository.findByName("CUSTOMER_UPDATE")
                .orElseGet(() -> permissionRepository.save(new Permission("CUSTOMER_UPDATE")));

            Permission deleteCustomer = permissionRepository.findByName("CUSTOMER_DELETE")
                .orElseGet(() -> permissionRepository.save(new Permission("CUSTOMER_DELETE")));

            //___________________________________________________________________________________________

            //Staff
            Permission createStaff = permissionRepository.findByName("STAFF_CREATE")
                .orElseGet(() -> permissionRepository.save(new Permission("STAFF_CREATE")));

            Permission readStaff = permissionRepository.findByName("STAFF_READ")
                .orElseGet(() -> permissionRepository.save(new Permission("STAFF_READ")));

            Permission updateStaff = permissionRepository.findByName("STAFF_UPDATE")
                .orElseGet(() -> permissionRepository.save(new Permission("STAFF_UPDATE")));

            Permission deleteStaff = permissionRepository.findByName("STAFF_DELETE")
                .orElseGet(() -> permissionRepository.save(new Permission("STAFF_DELETE")));


            //ROLES
            //Customer
            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseGet(() -> {
                    Role r = new Role("ROLE_CUSTOMER");

                    // Reservation -> alle Rechte
                    r.getPermissions().add(createReservation);
                    r.getPermissions().add(readReservation);
                    r.getPermissions().add(updateReservation);
                    r.getPermissions().add(deleteReservation);

                    // CustomerProfile -> alle Rechte
                    r.getPermissions().add(createCustomerProfile);
                    r.getPermissions().add(readCustomerProfile);
                    r.getPermissions().add(updateCustomerProfile);
                    r.getPermissions().add(deleteCustomerProfile);

                    // Equipment -> nur READ
                    r.getPermissions().add(readEquipment);

                    // Customer -> alle Rechte
                    r.getPermissions().add(readCustomer);
                    r.getPermissions().add(updateCustomer);
                    r.getPermissions().add(deleteCustomer);


                    return roleRepository.save(r);
                });

            //Staff
            Role staffRole = roleRepository.findByName("ROLE_STAFF")
                .orElseGet(() -> {
                    Role r = new Role("ROLE_STAFF");

                    // Equipment -> alles
                    r.getPermissions().add(createEquipment);
                    r.getPermissions().add(readEquipment);
                    r.getPermissions().add(updateEquipment);
                    r.getPermissions().add(deleteEquipment);

                    // Reservation -> nur READ
                    r.getPermissions().add(readReservation);

                    // Staff -> alles
                    r.getPermissions().add(createStaff);
                    r.getPermissions().add(readStaff);
                    r.getPermissions().add(updateStaff);
                    r.getPermissions().add(deleteStaff);

                    return roleRepository.save(r);
                });


            //TESTUSER
            //Customer
            Set<Role> rolesCustomer = Set.of(roleRepository.findByName("ROLE_CUSTOMER").orElseThrow());

            if (userRepository.findUserByEmail("hans.hansinger@email.com").isEmpty()) {
                userRepository.save(new Customer(
                    "Hans McHansFace",
                    encoder.encode("password"),
                    "hans.hansinger@email.com",
                    rolesCustomer,
                    new HashSet<>(),
                    "Hans",
                    "Hansinger",
                    LocalDate.of(1978, 5, 20)
                ));
            }

            //CustomerProfiles
            Customer customer = customerRepository.findByEmail("hans.hansinger@email.com").orElseThrow();

            if (profileRepository.findByCustomerAndProfileName(customer, "Hans").isEmpty()) {
                CustomerProfile profile1 = new CustomerProfile(
                    "Hans",
                    192.0,
                    86.4,
                    44,
                    SkillLevel.ADVANCED,
                    customer
                );

                profileRepository.save(profile1);
            }

            if (profileRepository.findByCustomerAndProfileName(customer, "Hansine").isEmpty()) {
                CustomerProfile profile2 = new CustomerProfile(
                    "Hansine",
                    165.0,
                    70.5,
                    38,
                    SkillLevel.INTERMEDIATE,
                    customer
                );

                profileRepository.save(profile2);
            }

            if (profileRepository.findByCustomerAndProfileName(customer, "Hans Junior").isEmpty()) {
                CustomerProfile profile3 = new CustomerProfile(
                    "Hans Junior",
                    120.0,
                    45.7,
                    36,
                    SkillLevel.BEGINNER,
                    customer
                );

                profileRepository.save(profile3);
            }


            //Staff
            Set<Role> rolesStaff =  Set.of(roleRepository.findByName("ROLE_STAFF").orElseThrow());

            if (userRepository.findUserByEmail("admin@email.com").isEmpty()) {
                userRepository.save(new Staff(
                    "Admin",
                    encoder.encode("password"),
                    "admin@email.com",
                    rolesStaff,
                    new HashSet<>()
                ));
            }

        };
    }
}