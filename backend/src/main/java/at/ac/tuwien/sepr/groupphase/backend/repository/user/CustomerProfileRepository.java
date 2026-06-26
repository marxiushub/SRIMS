package at.ac.tuwien.sepr.groupphase.backend.repository.user;

import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    Optional<CustomerProfile> findByCustomerAndProfileName(Customer customer, String profileName);

    List<CustomerProfile> findByCustomerId(Long customerId);

    Optional<CustomerProfile> findByProfileNameAndCustomerEmail(String userName, String profileName);

    Optional<CustomerProfile> findByIdAndCustomerId(Long id, Long customerId);

    boolean existsByCustomerIdAndProfileName(Long customerId, String profileName);

    boolean existsByCustomerIdAndProfileNameAndIdNot(Long customerId, String profileName, Long id);
}
