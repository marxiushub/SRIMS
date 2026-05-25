package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileCreationDto;

/**
 * Service interface for managing customer profile-related operations.
 */
public interface CustomerProfileService {

    /**
     * Creates a new customer profile for an existing customer.
     *
     * @param dto the data transfer object containing the information needed to create the customer profile
     * @return a {@link CustomerProfileDetailDto} representing the created customer profile
     */
    CustomerProfileDetailDto createCustomerProfile(CustomerProfileCreationDto dto);
}
