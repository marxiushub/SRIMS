package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.searchresponse;

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
    @JsonSubTypes.Type(value = CustomerSearchResponseDto.class, name = "CUSTOMER"),
    @JsonSubTypes.Type(value = StaffSearchResponseDto.class, name = "STAFF")
})
public class UserSearchResponseDto {

    private Long id;
    private String userName;
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

    public void setUserName(String userName) {
        this.userName = userName;
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
