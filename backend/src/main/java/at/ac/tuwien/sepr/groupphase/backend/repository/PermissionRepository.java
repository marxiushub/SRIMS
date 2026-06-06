package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    /**
     * Checks if a permission with given name is present in the repository.
     *
     * @param name name of the permission to be checked.
     * @return Permission if the permission was found in the repository
     */
    Optional<Permission> findByName(String name);
}
