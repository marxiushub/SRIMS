package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository repo, PasswordEncoder encoder) {
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

            if (repo.findUserByEmail("admin@email.com").isEmpty()) {
                repo.save(new Staff(
                    "Adrian",
                    encoder.encode("password"),
                    "admin@email.com"
                ));
            }
        };
    }
}