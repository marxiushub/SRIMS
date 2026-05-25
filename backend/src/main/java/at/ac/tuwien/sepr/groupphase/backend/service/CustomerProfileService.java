package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileUpdateDto;

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

    /**
     * Partially updates an existing customer profile.
     * Only the fields provided in the update DTO will be applied.
     *
     * @param customerProfileId the ID of the customer profile to update
     * @param dto the data transfer object containing the new values
     * @return a {@link CustomerProfileDetailDto} representing the updated customer profile
     */
    CustomerProfileDetailDto updateCustomerProfile(Long customerProfileId, CustomerProfileUpdateDto dto);
}
