package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

/**
 * DTO for updating customer users.
 */

public class CustomerUpdateDto extends UserUpdateDto {

    @NotBlank(message = "First name is empty")
    private String firstName;

    @NotBlank(message = "Last name is empty")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    // Constructors
    public CustomerUpdateDto() {
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

}
