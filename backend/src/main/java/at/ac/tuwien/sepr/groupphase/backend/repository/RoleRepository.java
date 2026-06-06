package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Checks if a Role with given name is present in the repository.
     *
     * @param name name of the Role to be checked.
     * @return Role if the permission was found in the repository
     */
    Optional<Role> findByName(String name);
}
