package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository repo, PasswordEncoder encoder, CustomerRepository customerRepository, CustomerProfileRepository profileRepository) {
        return args -> {

            if (repo.findUserByEmail("hans.hansinger@email.com").isEmpty()) {
                repo.save(new Customer(
                    "Hans McHansFace",
                    encoder.encode("password"),
                    "hans.hansinger@email.com",
                    "Hans",
                    "Hansinger",
                    LocalDate.of(1978, 5, 20)
                ));
            }

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


            if (repo.findUserByEmail("admin@email.com").isEmpty()) {
                repo.save(new Staff(
                    "Admin",
                    encoder.encode("password"),
                    "admin@email.com"
                ));
            }
        };
    }
}