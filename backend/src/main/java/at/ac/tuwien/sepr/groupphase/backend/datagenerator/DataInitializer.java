package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository repo, PasswordEncoder encoder) {
        return args -> {

            if (repo.findUserByEmail("userkev@email.com").isEmpty()) {
                repo.save(new ApplicationUser(
                    "userkev@email.com",
                    encoder.encode("password"),
                    false,
                    "kev",
                    SkillLevel.BEGINNER
                ));
            }

            if (repo.findUserByEmail("admin@email.com").isEmpty()) {
                repo.save(new ApplicationUser(
                    "admin@email.com",
                    encoder.encode("password"),
                    true,
                    "Adrian",
                    SkillLevel.ADVANCED
                ));
            }
        };
    }
}