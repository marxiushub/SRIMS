package at.ac.tuwien.sepr.groupphase.backend.repository.user;

import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
}
