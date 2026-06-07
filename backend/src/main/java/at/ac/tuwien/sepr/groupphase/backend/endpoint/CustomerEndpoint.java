package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.CustomerCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.searchresponse.UserSearchResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.CustomerUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.CustomUserDetailService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

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
    public UserDetailDto createCustomer(@Valid @RequestBody CustomerCreationDto dto) {
        LOGGER.info("POST /api/v1/customer/create - {}", dto);
        return userService.createUser(dto);
    }

    /**
     * Endpoint to update an existing Customer account.
     *
     * @param id the ID of the customer user to update
     * @param dto the DTO containing updated customer data
     * @return the updated customer user
     */
    @PreAuthorize("hasAnyAuthority('CUSTOMER_UPDATE')")
    @PutMapping("/update/{id}")
    public UserDetailDto updateCustomer(@PathVariable("id") Long id, @Valid @RequestBody CustomerUpdateDto dto) {
        LOGGER.info("PUT /api/v1/customer/update/{} - {}", id, dto);
        return userService.updateUser(id, dto);
    }

    /**
     * Endpoint to delete an existing Customer account.
     *
     * @param id the ID of the Costumer user to delete
     */
    @PreAuthorize("hasAnyAuthority('CUSTOMER_DELETE')")
    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        LOGGER.info("DELETE /api/v1/costumer/delete/{}", id);
        userService.deleteUserById(id);
    }

    /**
     * Endpoint to retrieve a customer account.
     *
     * @param id the user of the customer that should be retrieved
     * @return the user as UserSearchResponseDto
     */
    @PreAuthorize("hasAnyAuthority('CUSTOMER_READ')")
    @GetMapping("/{id}")
    public UserSearchResponseDto getUserById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/customer/{}", id);
        return userService.getUserById(id);
    }
}
