package at.ac.tuwien.sepr.groupphase.backend.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import jakarta.persistence.GenerationType;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

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

    @Column(nullable = false, unique = false)
    private String userName;

    @Column(nullable = false, unique = true)
    private String hashedPassword;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Boolean admin;

    /**
     * empty constructor,for jpa.
     *
     */
    protected ApplicationUser() {
    }

    public ApplicationUser(String email, String hashedPassword, Boolean admin, String userName) {
        this.userName = userName;
        this.hashedPassword = hashedPassword;
        this.email = email;
        this.admin = admin;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public String getPassword() {
        return hashedPassword;
    }

    public Long getId() {
        return id;
    }


}