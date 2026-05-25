package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.StaffRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;

@Profile({"generateData", "datagenerator"})
@Component
public class UserDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CustomerRepository customerRepository;
    private final CustomerProfileRepository profileRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    private final List<Customer> customers;
    private final List<CustomerProfile> profiles;
    private final List<Staff> staffMembers;

    public UserDataGenerator(CustomerRepository customerRepository, CustomerProfileRepository profileRepository, StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.profileRepository = profileRepository;
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;

        this.customers = buildCustomers();
        this.profiles = buildProfiles();
        this.staffMembers = buildStaff();
    }

    // Customer
    private List<Customer> buildCustomers() {
        return List.of(

            new Customer(
                "retroGamer92",
                passwordEncoder.encode("superMario92"),
                "marcel.neumann@example.com",
                "Marcel",
                "Neumann",
                LocalDate.of(1992, 2, 14)
            ),

            new Customer(
                "coffee_addict",
                passwordEncoder.encode("espressoLover!"),
                "sarah.krueger@example.com",
                "Sarah",
                "Krüger",
                LocalDate.of(1987, 10, 3)
            ),

            new Customer(
                "hiking_tom",
                passwordEncoder.encode("mountainTrail77"),
                "tom.hartwig@example.com",
                "Tom",
                "Hartwig",
                LocalDate.of(1998, 6, 21)
            ),

            new Customer(
                "pixelqueen",
                passwordEncoder.encode("pinkPixel123"),
                "lena.schulze@example.com",
                "Lena",
                "Schulze",
                LocalDate.of(2001, 12, 11)
            ),

            new Customer(
                "chef_on_fire",
                passwordEncoder.encode("pastaPassion"),
                "giuseppe.romano@example.com",
                "Giuseppe",
                "Romano",
                LocalDate.of(1983, 4, 8)
            ),

            new Customer(
                "nightowl_dev",
                passwordEncoder.encode("debugMeIfYouCan"),
                "nina.becker@example.com",
                "Nina",
                "Becker",
                LocalDate.of(1995, 9, 17)
            ),

            new Customer(
                "ski_freak",
                passwordEncoder.encode("snowboard2024"),
                "lukas.gruber@example.com",
                "Lukas",
                "Gruber",
                LocalDate.of(1990, 1, 29)
            ),

            new Customer(
                "bookworm_mia",
                passwordEncoder.encode("libraryQuiet99"),
                "mia.albrecht@example.com",
                "Mia",
                "Albrecht",
                LocalDate.of(1979, 7, 5)
            ),

            new Customer(
                "urban.nomad",
                passwordEncoder.encode("cityLights88"),
                "kevin.yilmaz@example.com",
                "Kevin",
                "Yılmaz",
                LocalDate.of(1999, 11, 23)
            ),

            new Customer(
                "vinyl_collector",
                passwordEncoder.encode("jazzAndRecords"),
                "hannah.falk@example.com",
                "Hannah",
                "Falk",
                LocalDate.of(1985, 5, 30)
            )
        );
    }

    // Profiles
    private List<CustomerProfile> buildProfiles() {
        return List.of(

            new CustomerProfile("Marcel Neumann", 180, 81, 42, SkillLevel.INTERMEDIATE, customers.get(0)),
            new CustomerProfile("Anna", 142, 36, 35, SkillLevel.BEGINNER, customers.get(0)),
            new CustomerProfile("Sabine", 179, 83, 42, SkillLevel.ADVANCED, customers.get(0)),

            new CustomerProfile("Sarah Krüger", 165, 59, 38, SkillLevel.INTERMEDIATE, customers.get(1)),
            new CustomerProfile("Emma Krüger", 130, 28, 33, SkillLevel.BEGINNER, customers.get(1)),
            new CustomerProfile("Sari", 166, 61, 38, SkillLevel.BEGINNER, customers.get(1)),

            new CustomerProfile("Tom Hartwig", 175, 75, 43, SkillLevel.ADVANCED, customers.get(2)),
            new CustomerProfile("Thomas H.", 176, 77, 43, SkillLevel.INTERMEDIATE, customers.get(2)),

            new CustomerProfile("Lena Schulze", 168, 57, 37, SkillLevel.INTERMEDIATE, customers.get(3)),
            new CustomerProfile("Leni", 167, 56, 37, SkillLevel.BEGINNER, customers.get(3)),

            new CustomerProfile("David Huber", 182, 87, 44, SkillLevel.ADVANCED, customers.get(4)),
            new CustomerProfile("Dave H.", 183, 88, 44, SkillLevel.INTERMEDIATE, customers.get(4)),

            new CustomerProfile("Giuseppe Romano", 171, 93, 41, SkillLevel.INTERMEDIATE, customers.get(5)),
            new CustomerProfile("Marco Romano", 156, 49, 39, SkillLevel.BEGINNER, customers.get(5)),
            new CustomerProfile("Chef Giuseppe", 170, 95, 41, SkillLevel.ADVANCED, customers.get(5)),

            new CustomerProfile("Lukas Gruber", 178, 80, 43, SkillLevel.INTERMEDIATE, customers.get(6)),
            new CustomerProfile("Luki", 179, 82, 43, SkillLevel.BEGINNER, customers.get(6)),

            new CustomerProfile("Mia Albrecht", 160, 53, 36, SkillLevel.BEGINNER, customers.get(7)),
            new CustomerProfile("Mimi Albrecht", 161, 55, 36, SkillLevel.INTERMEDIATE, customers.get(7)),

            new CustomerProfile("Kevin Yılmaz", 185, 89, 45, SkillLevel.INTERMEDIATE, customers.get(8)),

            new CustomerProfile("Hannah Falk", 167, 62, 39, SkillLevel.ADVANCED, customers.get(9))
        );
    }

    // Staff
    private List<Staff> buildStaff() {
        return List.of(

            new Staff(
                "admin.core",
                passwordEncoder.encode("adminSecure2026!"),
                "admin.core@system.com"
            ),

            new Staff(
                "support.meyer",
                passwordEncoder.encode("helpdesk42"),
                "support.meyer@system.com"
            ),

            new Staff(
                "it.schneider",
                passwordEncoder.encode("serverRoom#7"),
                "it.schneider@system.com"
            ),

            new Staff(
                "ops.wagner",
                passwordEncoder.encode("opsDailyRun"),
                "ops.wagner@system.com"
            ),

            new Staff(
                "lisa.hr",
                passwordEncoder.encode("peopleFirst!"),
                "lisa.hr@system.com"
            )
        );
    }


    @PostConstruct
    public void generateData() {
        generateCustomer();
        generateCustomerProfile();
        generateStaff();
    }

    public void generateCustomer() {
        if (!customerRepository.findAll().isEmpty()) {
            LOGGER.debug("Customer already generated");
            return;
        }
        LOGGER.debug("Generating " + customers.size() + " customers");
        customerRepository.saveAll(customers);
    }

    public void generateCustomerProfile() {
        if (!profileRepository.findAll().isEmpty()) {
            LOGGER.debug("Customer profiles already generated");
            return;
        }
        LOGGER.debug("Generating " + profiles.size() + " customer profiles");
        profileRepository.saveAll(profiles);
    }

    public void generateStaff() {
        if (!staffRepository.findAll().isEmpty()) {
            LOGGER.debug("Staff already generated");
            return;
        }
        LOGGER.debug("Generating " + staffMembers.size() + " staff members");
        staffRepository.saveAll(staffMembers);
    }
}