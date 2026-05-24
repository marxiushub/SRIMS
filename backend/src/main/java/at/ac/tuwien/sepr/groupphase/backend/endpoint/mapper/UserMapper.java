package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.CustomerDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.StaffDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;

/**
 * Mapper class responsible for converting between {@link ApplicationUser} entities and {@link UserDetailDto} data transfer objects.
 */
@Mapper(subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION, componentModel = "spring")
public interface UserMapper {

    /**
     * Converts an {@link ApplicationUser} entity to an {@link UserDetailDto}.
     *
     * @param user the {@link ApplicationUser} entity to be converted
     * @return the corresponding {@link UserDetailDto} containing the data from the provided entity
     */
    @SubclassMapping(source = Customer.class, target = CustomerDetailDto.class)
    @SubclassMapping(source = Staff.class, target = StaffDetailDto.class)
    UserDetailDto entityToDto(ApplicationUser user);

}
