package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.CustomerCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.creation.StaffCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.detail.UserDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.userdto.update.StaffUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.CustomUserDetailService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.lang.invoke.MethodHandles;

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
     * @param dto an {@link CustomerCreationDto}
     * @return a Staff entity
     */
    @PermitAll
    @PostMapping("/create")
    public UserDetailDto createEquipment(@Valid @RequestBody StaffCreationDto dto) {
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
    @PermitAll
    @PutMapping("/update/{id}")
    public UserDetailDto updateStaff(@PathVariable Long id, @Valid @RequestBody StaffUpdateDto dto) {
        LOGGER.info("PUT /api/v1/staff/update/{} - {}", id, dto);
        return userService.updateUser(id, dto);
    }
}
