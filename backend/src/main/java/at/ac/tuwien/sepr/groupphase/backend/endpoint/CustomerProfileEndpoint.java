package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.customerprofile.CustomerProfileCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.service.CustomerProfileService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;


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

    @PermitAll
    @PostMapping("/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerProfileDetailDto createCustomerProfile(@Valid @RequestBody CustomerProfileCreationDto dto) {
        LOGGER.info("POST /api/v1/customer/profiles - {}", dto);
        return customerProfileService.createCustomerProfile(dto);
    }

    @PermitAll
    @GetMapping("/{customerId}/profiles")
    @ResponseStatus(HttpStatus.OK)
    public List<CustomerProfileDetailDto> getCustomerProfiles(@PathVariable("customerId") Long customerId) {
        LOGGER.info("GET /api/v1/customer/{}/profiles", customerId);
        return customerProfileService.getCustomerProfiles(customerId);
    }
}
