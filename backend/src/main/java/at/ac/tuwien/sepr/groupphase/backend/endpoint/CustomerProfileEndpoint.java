package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.CustomerProfileService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;


import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Represents the REST API endpoint for managing customer profile-related operations.
 *
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerProfileEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CustomerProfileService customerProfileService;

    public CustomerProfileEndpoint(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    /**
     * Endpoint to create a new CustomerProfile for the authenticated customer.
     *
     * @param dto A DTO containing the details of the CustomerProfile to create.
     * @return The details of the newly created CustomerProfile.
     */
    @PreAuthorize("hasAuthority('CUSTOMERPROFILE_CREATE')")
    @PostMapping("/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerProfileDetailDto createCustomerProfile(@Valid @RequestBody CustomerProfileCreationDto dto) {
        LOGGER.info("POST /api/v1/customer/profiles - {}", dto);
        return customerProfileService.createCustomerProfile(dto);
    }


    /**
     * Endpoint to retrieve all CustomerProfiles belonging to the authenticated customer.
     *
     * @return A list of CustomerProfiles owned by the authenticated customer.
     */
    @PreAuthorize("hasAuthority('CUSTOMERPROFILE_READ')")
    @GetMapping("/profiles")
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerProfileDetailDto> getCustomerProfiles() {
        LOGGER.info("GET /api/v1/customer/profiles");
        return customerProfileService.getCustomerProfiles();
    }

    /**
     * Endpoint for staff members to retrieve all CustomerProfiles belonging to a specific customer.
     *
     * @param customerId The ID of the customer whose CustomerProfiles should be retrieved.
     * @return A list of CustomerProfiles belonging to the specified customer.
     */
    @PreAuthorize("hasAuthority('CUSTOMERPROFILE_READ') and hasAuthority('STAFF')")
    @GetMapping("/{customerId}/profiles")
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerProfileDetailDto> getCustomerProfiles(@PathVariable("customerId") Long customerId) {
        LOGGER.info("GET /api/v1/customer/{}/profiles", customerId);
        return customerProfileService.getCustomerProfiles(customerId);
    }

    /**
     * Endpoint to retrieve a specific CustomerProfile.
     * Customers may only access CustomerProfiles that belong to them,
     * while staff members may access any CustomerProfile.
     *
     * @param profileId The ID of the CustomerProfile to retrieve.
     * @return The details of the requested CustomerProfile.
     */
    @PreAuthorize("hasAuthority('CUSTOMERPROFILE_READ') or hasAuthority('STAFF')")
    @GetMapping("/profiles/{profileId}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerProfileDetailDto getCustomerProfileById(@PathVariable("profileId") Long profileId) {
        LOGGER.info("GET /api/v1/customer/profiles/{}", profileId);
        return customerProfileService.getCustomerProfileById(profileId);
    }

    /**
     * Endpoint to delete a CustomerProfile.
     *
     * @param profileId The ID of the CustomerProfile to delete.
     */
    @PreAuthorize("hasAuthority('CUSTOMERPROFILE_DELETE')")
    @DeleteMapping("/profiles/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomerProfile(@PathVariable("profileId") Long profileId) {
        LOGGER.info("DELETE /api/v1/customer/{}/profiles", profileId);
        customerProfileService.deleteCustomerProfile(profileId);
    }

    /**
     * Endpoint to update an existing CustomerProfile.
     *
     * @param profileId The ID of the CustomerProfile to update.
     * @param dto A DTO containing the updated CustomerProfile information.
     * @return The updated details of the CustomerProfile.
     */
    @PreAuthorize("hasAuthority('CUSTOMERPROFILE_UPDATE')")
    @PatchMapping("/profiles/{profileId}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerProfileDetailDto updateCustomerProfile(@PathVariable("profileId") Long profileId, @Valid @RequestBody CustomerProfileUpdateDto dto) {
        LOGGER.info("PATCH /api/v1/customer/profiles/{} - {}", profileId, dto);

        return customerProfileService.updateCustomerProfile(profileId, dto);
    }
}
