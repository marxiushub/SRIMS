package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.CustomerDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.StaffDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.CustomerUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.StaffUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION
)
public interface UserMapper {


    //Entity => DetailDto (read)
    @SubclassMapping(source = Customer.class, target = CustomerDetailDto.class)
    @SubclassMapping(source = Staff.class, target = StaffDetailDto.class)
    UserDetailDto entityToDto(ApplicationUser user);

    //Update
    //UserType specific
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCustomer(CustomerUpdateDto dto, @MappingTarget Customer entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStaff(StaffUpdateDto dto, @MappingTarget Staff entity);

    default void updateEntityFromDto(UserUpdateDto dto, @MappingTarget ApplicationUser entity) {

        if (dto == null || entity == null) {
            return;
        }

        switch (dto) {
            case CustomerUpdateDto customerDto when entity instanceof Customer customer ->
                updateCustomer(customerDto, customer);

            case StaffUpdateDto staffDto when entity instanceof Staff staff ->
                updateStaff(staffDto, staff);

            default -> throw new IllegalArgumentException(
                "DTO type does not match entity type"
            );
        }
    }
}