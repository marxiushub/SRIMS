package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.CustomerDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.StaffDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.searchresponse.CustomerSearchResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.searchresponse.StaffSearchResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.searchresponse.UserSearchResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.CustomerUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.StaffUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Permission;
import at.ac.tuwien.sepr.groupphase.backend.entity.Role;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.UserType;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Mapping;
import org.mapstruct.AfterMapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
    componentModel = "spring",
    subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION
)
public interface UserMapper {


    //Entity => DetailDto (create)
    //Entity => DetailDto (create)
    @SubclassMapping(source = Customer.class, target = CustomerDetailDto.class)
    @SubclassMapping(source = Staff.class, target = StaffDetailDto.class)
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "directPermissions", source = "directPermissions")
    @Mapping(target = "userType", ignore = true)
    UserDetailDto entityToDetailDto(ApplicationUser user);

    @AfterMapping
    default void setUserTypeDetail(ApplicationUser user, @MappingTarget UserDetailDto dto) {
        if (user instanceof Customer) {
            dto.setUserType(UserType.CUSTOMER);
        } else if (user instanceof Staff) {
            dto.setUserType(UserType.STAFF);
        }
    }

    //Update
    //UserType specific
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCustomer(CustomerUpdateDto dto, @MappingTarget Customer entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStaff(StaffUpdateDto dto, @MappingTarget Staff entity);

    //General Fields in User:
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "password", ignore = true)
    default void updateEntityFromDto(UserUpdateDto dto, @MappingTarget ApplicationUser entity) {

        if (dto == null || entity == null) {
            return;
        }

        if (dto instanceof CustomerUpdateDto customerDto && entity instanceof Customer customer) {
            updateCustomer(customerDto, customer);
            return;
        }

        if (dto instanceof StaffUpdateDto staffDto && entity instanceof Staff staff) {
            updateStaff(staffDto, staff);
            return;
        }

        throw new IllegalArgumentException("DTO type does not match entity type");
    }


    //Entity => SearchResponseDto
    @SubclassMapping(source = Customer.class, target = CustomerSearchResponseDto.class)
    @SubclassMapping(source = Staff.class, target = StaffSearchResponseDto.class)
    @Mapping(target = "userType", ignore = true)
    UserSearchResponseDto entityToSearchResponseDto(ApplicationUser user);

    @AfterMapping
    default void setUserTypeSearch(ApplicationUser user, @MappingTarget UserSearchResponseDto dto) {
        if (user instanceof Customer) {
            dto.setUserType(UserType.CUSTOMER);
        } else if (user instanceof Staff) {
            dto.setUserType(UserType.STAFF);
        }
    }


    // Helper methods for mapping roles & permissions -> List<String>
    default List<String> mapRolesToNames(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream().map(Role::getName).collect(Collectors.toList());
    }

    default List<String> mapPermissionsToNames(Set<Permission> permissions) {
        if (permissions == null) {
            return null;
        }
        return permissions.stream().map(Permission::getName).collect(Collectors.toList());
    }

    // Helper to determine UserType dynamically
    default UserType determineUserType(ApplicationUser applicationUser) {
        if (applicationUser instanceof Customer) {
            return UserType.CUSTOMER;
        } else if (applicationUser instanceof Staff) {
            return UserType.STAFF;
        } else {
            return null;
        }
    }

}