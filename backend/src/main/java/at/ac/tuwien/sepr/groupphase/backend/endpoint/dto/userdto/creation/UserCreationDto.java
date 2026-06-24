package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.UserType;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Represents a Dto for creating users.
 */

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CustomerCreationDto.class, name = "CUSTOMER"),
    @JsonSubTypes.Type(value = StaffCreationDto.class, name = "STAFF")
})


public abstract class UserCreationDto {

    @NotBlank(message = "Profile name is empty")
    private String userName;

    @NotBlank(message = "Password is empty")
    @Size(min = 10, message = "Password must at least contain 10 characters ")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
        message = "Password must contain at least one number, and one special characters."
    )
    private String password;

    @NotBlank(message = "E-Mail is empty")
    @Email(message = "Invalid E-Mail format")
    private String email;

    //Getter and Setter
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    //Helper methods:
    public abstract UserType getType();

    public abstract ApplicationUser toEntity();
}
