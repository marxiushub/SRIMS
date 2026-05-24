package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.CustomerCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.CustomUserDetailService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

/**
 * Represents the REST API endpoint for managing customer-related operations.
 * Provides endpoint for retrieving equipment information.
 */
@RestController
@RequestMapping("/api/v1/customer")
public class CustomerEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CustomUserDetailService userService;

    @Autowired
    public CustomerEndpoint(CustomUserDetailService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint to create a new Customer account.
     *
     * @param dto an {@link CustomerCreationDto}
     * @return a Customer entity
     */
    @PermitAll
    @PostMapping("/create")
    public UserDetailDto createEquipment(@Valid @RequestBody CustomerCreationDto dto) {
        LOGGER.info("POST /api/v1/customer/create - {}", dto);
        return userService.createUser(dto);
    }
}
