package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.UserType;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) for representing the details of a customer account.
 * Extends the base {@link UserDetailDto} to include specific attributes related to customer accounts.
 */
public class CustomerDetailDto extends UserDetailDto {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;


    /**
     * Getter and setter.
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


}
