package at.ac.tuwien.sepr.groupphase.backend.entity.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Customer extends ApplicationUser {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerProfile> profiles = new ArrayList<>();

    protected Customer() {}

    public Customer(String userName, String hashedPassword, String email, String firstName, String lastName, LocalDate dateOfBirth) {
        super(email, hashedPassword, false, userName);
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.profiles = new ArrayList<>();
    }

    /**
     * Getter and setter.
     *
     */
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<CustomerProfile> getProfiles() {
        return profiles;
    }

}
