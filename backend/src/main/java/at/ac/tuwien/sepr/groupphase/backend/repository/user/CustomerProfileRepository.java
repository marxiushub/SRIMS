package at.ac.tuwien.sepr.groupphase.backend.repository.user;

import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    Optional<CustomerProfile> findByCustomerAndProfileName(Customer customer, String profileName);
}
