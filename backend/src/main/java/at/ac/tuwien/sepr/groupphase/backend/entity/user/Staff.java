package at.ac.tuwien.sepr.groupphase.backend.entity.user;

import at.ac.tuwien.sepr.groupphase.backend.entity.Permission;
import at.ac.tuwien.sepr.groupphase.backend.entity.Role;
import jakarta.persistence.Entity;

import java.util.Set;

@Entity
public class Staff extends ApplicationUser {

    protected Staff() {}

    public  Staff(String userName, String hashedPassword, String email, Set<Role> roles, Set<Permission> directPermissions) {
        super(userName, hashedPassword, email,  roles, directPermissions);
    }
}
