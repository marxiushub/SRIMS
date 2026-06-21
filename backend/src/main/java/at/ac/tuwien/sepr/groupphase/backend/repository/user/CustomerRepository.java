package at.ac.tuwien.sepr.groupphase.backend.repository.user;

import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);

    @Query("""
    SELECT c
    FROM Customer c
    WHERE (:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%')))
    AND (:userName IS NULL OR LOWER(c.userName) LIKE LOWER(CONCAT('%', :userName, '%')))
    AND (:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')))
    AND (:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))
        """)

    List<Customer> searchCustomers(
        @Param("email") String email,
        @Param("userName") String userName,
        @Param("firstName") String firstName,
        @Param("lastName") String lastName
    );
}
