package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.UserType;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;

/**
 * Dto for creating Customers.
 */

public class CustomerCreationDto extends UserCreationDto {
    @NotBlank(message = "First name is empty")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is empty")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    // Constructors
    public CustomerCreationDto() {
    }

    // Getter and Setter
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


    @Override
    public UserType getType() {
        return UserType.CUSTOMER;
    }

    @Override
    public Customer toEntity() {
        return new Customer(getUserName(), getPassword(), getEmail(), new HashSet<>(), new HashSet<>(), getFirstName(), getLastName(), getDateOfBirth());
    }
}
