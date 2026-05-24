package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.UserType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents a Data Transfer Object (DTO) for user information. This class
 * is used to transfer user data between different layers of the application.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "userType",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CustomerDetailDto.class, name = "CUSTOMER"),
    @JsonSubTypes.Type(value = StaffDetailDto.class, name = "STAFF")
})

public class UserDetailDto {
    private Long id;
    private String userName;
    private String password;
    private String email;
    private UserType userType;

    /**
     * Getter and setter.
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
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

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
