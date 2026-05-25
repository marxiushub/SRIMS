package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.searchresponse;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.UserType;

public class StaffSearchResponseDto extends UserSearchResponseDto {
    @Override
    public UserType getUserType() {
        return UserType.STAFF;
    }
}
