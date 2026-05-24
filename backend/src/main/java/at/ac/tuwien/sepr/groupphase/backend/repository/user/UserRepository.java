package at.ac.tuwien.sepr.groupphase.backend.repository.user;

import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<ApplicationUser, Long>, JpaSpecificationExecutor<ApplicationUser> {
    Optional<ApplicationUser> findUserByEmail(String email);
}
