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
 * TODO: Müssen noch die Pfade ändern, wenn wir mit den Tokens arbeiten und nicht mehr Ids übergeben
 */
@RestController
@RequestMapping("/api/v1/customer")
public class CustomerProfileEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CustomerProfileService customerProfileService;

    public CustomerProfileEndpoint(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    @PreAuthorize("hasAuthority('CUSTOMERPROFILE_CREATE')")
    @PostMapping("/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerProfileDetailDto createCustomerProfile(@Valid @RequestBody CustomerProfileCreationDto dto) {
        LOGGER.info("POST /api/v1/customer/profiles - {}", dto);
        return customerProfileService.createCustomerProfile(dto);
    }

    //Allows Customers to Read all of their CustomerProfiles
    @PreAuthorize("hasAuthority('CUSTOMERPROFILE_READ')")
    @GetMapping("/profiles")
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerProfileDetailDto> getCustomerProfiles() {
        LOGGER.info("GET /api/v1/customer/profiles");
        return customerProfileService.getCustomerProfiles();
    }

    //Allows Staff to Read all of their CustomerProfiles
    @PreAuthorize("hasAuthority('CUSTOMERPROFILE_READ') and hasAuthority('STAFF')")
    @GetMapping("/{customerId}/profiles")
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerProfileDetailDto> getCustomerProfiles(@PathVariable("customerId") Long customerId) {
        LOGGER.info("GET /api/v1/customer/{}/profiles", customerId);
        return customerProfileService.getCustomerProfiles(customerId);
    }

    //Allows Staff to read a specific CustomerProfiles when a valid CustomerProfileID is provided
    //Allows Customer to read a specific CustomerProfile, that belongs to the Customer sending the Request
    @PreAuthorize("hasAuthority('CUSTOMERPROFILE_READ') or hasAuthority('STAFF')")
    @GetMapping("/profiles/{profileId}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerProfileDetailDto getCustomerProfileById(@PathVariable("profileId") Long profileId) {
        LOGGER.info("GET /api/v1/customer/profiles/{}", profileId);
        return customerProfileService.getCustomerProfileById(profileId);
    }

    @PreAuthorize("hasAuthority('CUSTOMERPROFILE_DELETE')")
    @DeleteMapping("/profiles/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomerProfile(@PathVariable("profileId") Long profileId) {
        LOGGER.info("DELETE /api/v1/customer/{}/profiles", profileId);
        customerProfileService.deleteCustomerProfile(profileId);
    }

    @PreAuthorize("hasAuthority('CUSTOMERPROFILE_UPDATE')")
    @PatchMapping("/profiles/{profileId}")
    @ResponseStatus(HttpStatus.OK)
    public CustomerProfileDetailDto updateCustomerProfile(@PathVariable("profileId") Long profileId, @Valid @RequestBody CustomerProfileUpdateDto dto) {
        LOGGER.info("PATCH /api/v1/customer/profiles/{} - {}", profileId, dto);

        return customerProfileService.updateCustomerProfile(profileId, dto);
    }
}
