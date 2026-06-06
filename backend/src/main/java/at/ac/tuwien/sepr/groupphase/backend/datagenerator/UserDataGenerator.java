package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.Role;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Profile({"generateData", "datagenerator"})
@Component
@Order(2)
public class UserDataGenerator implements CommandLineRunner {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CustomerRepository customerRepository;
    private final CustomerProfileRepository profileRepository;
    private final StaffRepository staffRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataGenerator(
        CustomerRepository customerRepository,
        CustomerProfileRepository profileRepository,
        StaffRepository staffRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.customerRepository = customerRepository;
        this.profileRepository = profileRepository;
        this.staffRepository = staffRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
            .orElseThrow(() -> new IllegalStateException("ROLE_CUSTOMER not found"));

        Role staffRole = roleRepository.findByName("ROLE_STAFF")
            .orElseThrow(() -> new IllegalStateException("ROLE_STAFF not found"));

        generateCustomers(customerRole);
        generateStaff(staffRole);
        generateProfiles();
    }


    //CUSTOMER
    private void generateCustomers(Role customerRole) {

        if (customerRepository.findByEmail("marcel.neumann@example.com").isPresent()) {
            LOGGER.debug("Customers already generated");
            return;
        }

        LOGGER.debug("Generating customers");

        List<Customer> customers = List.of(

            new Customer(
                "retroGamer92",
                passwordEncoder.encode("superMario92"),
                "marcel.neumann@example.com",
                Set.of(customerRole),
                Set.of(),
                "Marcel",
                "Neumann",
                LocalDate.of(1992, 2, 14)
            ),

            new Customer(
                "coffee_addict",
                passwordEncoder.encode("espressoLover!"),
                "sarah.krueger@example.com",
                Set.of(customerRole),
                Set.of(),
                "Sarah",
                "Krüger",
                LocalDate.of(1987, 10, 3)
            ),

            new Customer(
                "hiking_tom",
                passwordEncoder.encode("mountainTrail77"),
                "tom.hartwig@example.com",
                Set.of(customerRole),
                Set.of(),
                "Tom",
                "Hartwig",
                LocalDate.of(1998, 6, 21)
            ),

            new Customer(
                "pixelqueen",
                passwordEncoder.encode("pinkPixel123"),
                "lena.schulze@example.com",
                Set.of(customerRole),
                Set.of(),
                "Lena",
                "Schulze",
                LocalDate.of(2001, 12, 11)
            ),

            new Customer(
                "chef_on_fire",
                passwordEncoder.encode("pastaPassion"),
                "giuseppe.romano@example.com",
                Set.of(customerRole),
                Set.of(),
                "Giuseppe",
                "Romano",
                LocalDate.of(1983, 4, 8)
            ),

            new Customer(
                "nightowl_dev",
                passwordEncoder.encode("debugMeIfYouCan"),
                "nina.becker@example.com",
                Set.of(customerRole),
                Set.of(),
                "Nina",
                "Becker",
                LocalDate.of(1995, 9, 17)
            ),

            new Customer(
                "ski_freak",
                passwordEncoder.encode("snowboard2024"),
                "lukas.gruber@example.com",
                Set.of(customerRole),
                Set.of(),
                "Lukas",
                "Gruber",
                LocalDate.of(1990, 1, 29)
            ),

            new Customer(
                "bookworm_mia",
                passwordEncoder.encode("libraryQuiet99"),
                "mia.albrecht@example.com",
                Set.of(customerRole),
                Set.of(),
                "Mia",
                "Albrecht",
                LocalDate.of(1979, 7, 5)
            ),

            new Customer(
                "urban.nomad",
                passwordEncoder.encode("cityLights88"),
                "kevin.yilmaz@example.com",
                Set.of(customerRole),
                Set.of(),
                "Kevin",
                "Yılmaz",
                LocalDate.of(1999, 11, 23)
            ),

            new Customer(
                "vinyl_collector",
                passwordEncoder.encode("jazzAndRecords"),
                "hannah.falk@example.com",
                Set.of(customerRole),
                Set.of(),
                "Hannah",
                "Falk",
                LocalDate.of(1985, 5, 30)
            )
        );

        customerRepository.saveAll(customers);
    }



    //STAFF
    private void generateStaff(Role staffRole) {

        if (staffRepository.findByEmail("admin.core@system.com").isPresent()) {
            LOGGER.debug("Staff already generated");
            return;
        }

        LOGGER.debug("Generating staff");

        List<Staff> staffMembers = List.of(

            new Staff(
                "admin.core",
                passwordEncoder.encode("adminSecure2026!"),
                "admin.core@system.com",
                Set.of(staffRole),
                Set.of()
            ),

            new Staff(
                "support.meyer",
                passwordEncoder.encode("helpdesk42"),
                "support.meyer@system.com",
                Set.of(staffRole),
                Set.of()
            ),

            new Staff(
                "it.schneider",
                passwordEncoder.encode("serverRoom#7"),
                "it.schneider@system.com",
                Set.of(staffRole),
                Set.of()
            ),

            new Staff(
                "ops.wagner",
                passwordEncoder.encode("opsDailyRun"),
                "ops.wagner@system.com",
                Set.of(staffRole),
                Set.of()
            ),

            new Staff(
                "lisa.hr",
                passwordEncoder.encode("peopleFirst!"),
                "lisa.hr@system.com",
                Set.of(staffRole),
                Set.of()
            )
        );

        staffRepository.saveAll(staffMembers);
    }


    //PROFILES
    private void generateProfiles() {

        if (profileRepository.findByProfileNameAndCustomerEmail("Marcel Neumann", "marcel.neumann@example.com").isPresent()) {
            LOGGER.debug("Customer profiles already generated");
            return;
        }

        LOGGER.debug("Generating customer profiles");

        Customer marcel = customerRepository.findByEmail("marcel.neumann@example.com").orElseThrow();
        Customer sarah = customerRepository.findByEmail("sarah.krueger@example.com").orElseThrow();
        Customer tom = customerRepository.findByEmail("tom.hartwig@example.com").orElseThrow();
        Customer lena = customerRepository.findByEmail("lena.schulze@example.com").orElseThrow();
        Customer giuseppe = customerRepository.findByEmail("giuseppe.romano@example.com").orElseThrow();
        Customer nina = customerRepository.findByEmail("nina.becker@example.com").orElseThrow();
        Customer lukas = customerRepository.findByEmail("lukas.gruber@example.com").orElseThrow();
        Customer mia = customerRepository.findByEmail("mia.albrecht@example.com").orElseThrow();
        Customer kevin = customerRepository.findByEmail("kevin.yilmaz@example.com").orElseThrow();
        Customer hannah = customerRepository.findByEmail("hannah.falk@example.com").orElseThrow();

        List<CustomerProfile> profiles = List.of(

            new CustomerProfile("Marcel Neumann", 180, 81, 42, SkillLevel.INTERMEDIATE, marcel),
            new CustomerProfile("Anna", 142, 36, 35, SkillLevel.BEGINNER, marcel),
            new CustomerProfile("Sabine", 179, 83, 42, SkillLevel.ADVANCED, marcel),

            new CustomerProfile("Sarah Krüger", 165, 59, 38, SkillLevel.INTERMEDIATE, sarah),
            new CustomerProfile("Emma Krüger", 130, 28, 33, SkillLevel.BEGINNER, sarah),
            new CustomerProfile("Sari", 166, 61, 38, SkillLevel.BEGINNER, sarah),

            new CustomerProfile("Tom Hartwig", 175, 75, 43, SkillLevel.ADVANCED, tom),
            new CustomerProfile("Thomas H.", 176, 77, 43, SkillLevel.INTERMEDIATE, tom),

            new CustomerProfile("Lena Schulze", 168, 57, 37, SkillLevel.INTERMEDIATE, lena),
            new CustomerProfile("Leni", 167, 56, 37, SkillLevel.BEGINNER, lena),

            new CustomerProfile("David Huber", 182, 87, 44, SkillLevel.ADVANCED, giuseppe),
            new CustomerProfile("Dave H.", 183, 88, 44, SkillLevel.INTERMEDIATE, giuseppe),

            new CustomerProfile("Giuseppe Romano", 171, 93, 41, SkillLevel.INTERMEDIATE, giuseppe),
            new CustomerProfile("Marco Romano", 156, 49, 39, SkillLevel.BEGINNER, giuseppe),
            new CustomerProfile("Chef Giuseppe", 170, 95, 41, SkillLevel.ADVANCED, giuseppe),

            new CustomerProfile("Lukas Gruber", 178, 80, 43, SkillLevel.INTERMEDIATE, lukas),
            new CustomerProfile("Luki", 179, 82, 43, SkillLevel.BEGINNER, lukas),

            new CustomerProfile("Mia Albrecht", 160, 53, 36, SkillLevel.BEGINNER, mia),
            new CustomerProfile("Mimi Albrecht", 161, 55, 36, SkillLevel.INTERMEDIATE, mia),

            new CustomerProfile("Kevin Yılmaz", 185, 89, 45, SkillLevel.INTERMEDIATE, kevin),

            new CustomerProfile("Hannah Falk", 167, 62, 39, SkillLevel.ADVANCED, hannah)
        );

        profileRepository.saveAll(profiles);
    }
}