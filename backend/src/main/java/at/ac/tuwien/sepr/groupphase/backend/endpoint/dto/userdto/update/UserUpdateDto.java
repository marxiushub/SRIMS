package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CustomerUpdateDto.class, name = "CUSTOMER"),
    @JsonSubTypes.Type(value = StaffUpdateDto.class, name = "STAFF")
})


public abstract class UserUpdateDto {

    private String userName;

    @Size(min = 10, message = "Password must at least contain 10 characters ")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
        message = "Password must contain at least one number, and one special characters."
    )
    private String password;

    @Email(message = "Invalid E-Mail format")
    private String email;

    /**
     * Getter and setter.
     */

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
}
