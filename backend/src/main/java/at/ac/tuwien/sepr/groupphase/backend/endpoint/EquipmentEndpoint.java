package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Represents the REST API endpoint for managing equipment-related operations.
 * Provides endpoint for retrieving equipment information.
 */
@RestController
@RequestMapping("/api/v1/equipment")
public class EquipmentEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EquipmentService equipmentService;

    @Autowired
    public EquipmentEndpoint(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    /**
     * Endpoint to retrieve a list of all equipment. This endpoint is accessible to all users without authentication.
     *
     * @return a list of {@link EquipmentDetailDto} representing the equipment information
     */
    @PermitAll
    @GetMapping
    public List<EquipmentDetailDto> getAllEquipment() {
        LOGGER.info("GET /api/v1/equipment");
        return equipmentService.allEquipment();
    }

    /**
     * Endpoint to retrieve a list of equipment filtered by type. This endpoint is accessible to all users without authentication.
     *
     * @param type the type of equipment to filter by (e.g., "helmet", "ski", "snowboard")
     * @return a list of {@link EquipmentDetailDto} representing the equipment information for the specified type
     */
    @PermitAll
    @GetMapping("/{type}")
    public List<EquipmentDetailDto> getEquipmentByType(@PathVariable("type") String type) {
        LOGGER.info("GET /api/v1/equipment/{}", type);
        return equipmentService.equipmentByType(type);
    }
}
