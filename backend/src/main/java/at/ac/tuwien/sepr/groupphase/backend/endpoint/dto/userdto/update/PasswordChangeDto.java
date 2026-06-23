package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PasswordChangeDto {

    @NotNull(message = "Old password must not be null")
    private String oldPassword;

    @NotNull(message = "New password must not be null")
    @Size(min = 10, message = "Password must at least contain 10 characters ")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
        message = "Password must contain at least one number, and one special characters."
    )
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

}
