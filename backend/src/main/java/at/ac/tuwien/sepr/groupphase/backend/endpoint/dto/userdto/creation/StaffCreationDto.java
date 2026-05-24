package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.UserType;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;

/**
 * DTO for creating Staff users.
 */

public class StaffCreationDto extends UserCreationDto {

    public StaffCreationDto() {}

    @Override
    public UserType getType() {
        return  UserType.STAFF;
    }

    @Override
    public Staff toEntity() {
        return new Staff(getUserName(), getPassword(), getEmail());
    }
}