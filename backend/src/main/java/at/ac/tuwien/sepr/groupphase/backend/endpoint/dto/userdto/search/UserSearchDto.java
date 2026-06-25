package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.search;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserSearchDto {

    @Size(max = 100, message = "User name must not exceed 100 characters")
    private String userName;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "email must not exceed 255 characters")
    private String email;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
