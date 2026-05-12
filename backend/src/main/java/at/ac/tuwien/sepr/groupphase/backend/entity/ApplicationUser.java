package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import jakarta.persistence.GenerationType;

import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;

/**
 * This Class represents a user. A user is eather an admin or not.
 */

//PS: wie wir das genau mit dem admin account machen müss ma uns noch überlegen

@Entity
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

    @Enumerated(EnumType.STRING)
    private SkillLevel experience;

    /**
     * empty constructor,for jpa.
     *
     */
    protected ApplicationUser() {
    }

    public ApplicationUser(String email, String hashedPassword, Boolean admin, String userName, SkillLevel experience) {
        this.userName = userName;
        this.hashedPassword = hashedPassword;
        this.email = email;
        this.admin = admin;
        this.experience = experience;
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

    public SkillLevel getExperience() {
        return experience;
    }

    public Long getId() {
        return id;
    }


}