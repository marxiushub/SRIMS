package at.ac.tuwien.sepr.groupphase.backend.entity.user;

import at.ac.tuwien.sepr.groupphase.backend.entity.Permission;
import at.ac.tuwien.sepr.groupphase.backend.entity.Role;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.UserType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;



import jakarta.persistence.GenerationType;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * This Class represents a user. A user is eather an admin or not.
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    //For granting special and important rights to certain users only
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Permission> directPermissions = new HashSet<>();


    /**
     * empty constructor,for jpa.
     *
     */
    protected ApplicationUser() {
    }

    public ApplicationUser(String userName, String hashedPassword, String email, Set<Role> roles, Set<Permission> directPermissions) {
        this.userName = userName;
        this.hashedPassword = hashedPassword;
        this.email = email;
        this.roles = roles;
        this.directPermissions = directPermissions;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return hashedPassword;
    }

    public Long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Set<Permission> getDirectPermissions() {
        return directPermissions;
    }

}