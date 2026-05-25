package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting customer profile entities to DTOs.
 */
@Mapper(componentModel = "spring")
public interface CustomerProfileMapper {

    /**
     * Converts a {@link CustomerProfile} entity to a {@link CustomerProfileDetailDto}.
     *
     * @param customerProfile the customer profile entity to convert
     * @return the detail DTO
     */
    @Mapping(source = "customer.id", target = "customerId")
    CustomerProfileDetailDto entityToDetailDto(CustomerProfile customerProfile);
}
