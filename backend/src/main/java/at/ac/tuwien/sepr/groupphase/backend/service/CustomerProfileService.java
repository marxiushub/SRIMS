package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileCreationDto;

import java.util.List;

/**
 * Service interface for managing customer profile-related operations.
 */
public interface CustomerProfileService {

    /**
     * Deletes a customer profile by its ID.
     *
     * @param customerProfileId the ID of the customer profile to delete
     */
    void deleteCustomerProfile(Long customerProfileId);

    /**
     * Creates a new customer profile for an existing customer.
     *
     * @param dto the data transfer object containing the information needed to create the customer profile
     * @return a {@link CustomerProfileDetailDto} representing the created customer profile
     */
    CustomerProfileDetailDto createCustomerProfile(CustomerProfileCreationDto dto);

    /**
     * Retrieves all customer profiles belonging to a specific customer.
     *
     * @param customerId the ID of the customer whose profiles should be retrieved
     * @return a list if {@link CustomerProfileDetailDto} belonging to the customer
     */
    List<CustomerProfileDetailDto> getCustomerProfiles(Long customerId);
}
