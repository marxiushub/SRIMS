package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.StaffCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.search.CustomerSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.searchresponse.UserSearchResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.StaffUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.PasswordChangeDto;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.CustomUserDetailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PatchMapping;


import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Represents the REST API endpoint for managing customer-related operations.
 * Provides endpoint for retrieving equipment information.
 */
@RestController
@RequestMapping("/api/v1/staff")
public class StaffEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CustomUserDetailService userService;

    @Autowired
    public StaffEndpoint(CustomUserDetailService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint to create a new Staff account.
     *
     * @param dto an {@link StaffCreationDto}
     * @return a Staff entity
     */
    @PreAuthorize("hasAuthority('STAFF_CREATE')")
    @PostMapping("/create")
    public UserDetailDto createStaff(@Valid @RequestBody StaffCreationDto dto) {
        LOGGER.info("POST /api/v1/staff/create - {}", dto);
        return userService.createUser(dto);
    }

    /**
     * Endpoint to update an existing Staff account.
     *
     * @param id the ID of the staff user to update
     * @param dto the DTO containing updated staff data
     * @return the updated staff user
     */
    @PreAuthorize("hasAuthority('STAFF_UPDATE')")
    @PutMapping("/update/{id}")
    public UserDetailDto updateStaff(@PathVariable("id") Long id, @Valid @RequestBody StaffUpdateDto dto) {
        LOGGER.info("PUT /api/v1/staff/update/{} - {}", id, dto);
        return userService.updateUser(id, dto);
    }

    /**
     * Endpoint to change the password of an existing staff account.
     *
     * @param id the ID of the staff user whose password should be changed
     * @param dto the DTO containing the old and the new password
     * @return the updated staff user
     */
    @PreAuthorize("hasAuthority('STAFF_UPDATE')")
    @PatchMapping("/password/{id}")
    public UserDetailDto changePassword(@PathVariable("id") Long id, @Valid @RequestBody PasswordChangeDto dto) {
        LOGGER.info("PATCH /api/v1/staff/password/{} - {}", id, dto);
        return userService.changePassword(id, dto);
    }

    /**
     * Endpoint to delete an existing Staff account.
     *
     * @param id the ID of the staff user to delete
     */
    @PreAuthorize("hasAuthority('STAFF_DELETE')")
    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        LOGGER.info("DELETE /api/v1/staff/delete/{}", id);
        userService.deleteUserById(id);
    }

    /**
     * Endpoint to retrieve a staff account.
     *
     * @param id the user of the staff member that should be retrieved
     * @return the user as UserSearchResponseDto
     */
    @PreAuthorize("hasAuthority('STAFF_READ')")
    @GetMapping("/{id}")
    public UserSearchResponseDto getUserById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/staff/{}", id);
        return userService.getUserById(id);
    }

    /**
     * Searches for customers based on optional query parameters.
     * The parameters are passed in the URL.
     *
     * @param searchDto dynamically mapped from URL query parameters
     * @return a list of customers matching the criteria
     */
    @PreAuthorize("hasAuthority('CUSTOMER_READ') and hasAuthority('STAFF')")
    @GetMapping("/customers/search")
    @ResponseStatus(HttpStatus.OK)
    public List<UserSearchResponseDto> searchCustomers(CustomerSearchDto searchDto) {
        LOGGER.info("GET /api/v1/staff/customers/search");
        return userService.searchCustomers(searchDto);
    }
}
