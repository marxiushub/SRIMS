package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation.EquipmentCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.search.EquipmentSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update.EquipmentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PatchMapping;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
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
     * Endpoint to create a new Equipment.
     *
     * @param dto an {@link EquipmentCreationDto}
     * @return an Equipment entity
     *
     */
    @PermitAll
    @PostMapping()
    public List<EquipmentDetailDto> createEquipment(@Valid @RequestBody EquipmentCreationDto dto) {
        LOGGER.info("POST /api/v1/equipment - {}", dto);
        return equipmentService.createEquipment(dto);
    }

    /**
     * Endpoint to retrieve a list of equipment filtered by type. This endpoint is accessible to all users without authentication.
     *
     * @param type the type of equipment to filter by (e.g., "helmet", "ski", "snowboard")
     * @return a list of {@link EquipmentDetailDto} representing the equipment information for the specified type
     */
    @PermitAll
    @GetMapping("/type/{type}")
    public List<EquipmentDetailDto> getEquipmentByType(@PathVariable("type") String type) {
        LOGGER.info("GET /api/v1/equipment/type/{}", type);
        return equipmentService.equipmentByType(type);
    }

    /**
     * Endpoint to retrieve a specific piece of equipment by its unique ID. This endpoint is accessible to all users without authentication.
     *
     * @param id the unique ID of the equipment to be retrieved
     * @return an {@link EquipmentDetailDto} representing the equipment information for the specified ID
     */
    @PermitAll
    @GetMapping("/{id}")
    public EquipmentDetailDto getEquipmentById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/equipment/{}", id);
        return equipmentService.equipmentById(id);
    }

    /**
     * Endpoint to retrieve a specific piece of equipment by its unique barcodeId. This endpoint is accessible to all users without authentication.
     *
     * @param barcodeId the unique barcodeId of the equipment to be retrieved
     * @return an {@link EquipmentDetailDto} representing the equipment information for the specified barcodeId
     */
    @PermitAll
    @GetMapping("/barcode/{barcodeId}")
    public EquipmentDetailDto getEquipmentById(@PathVariable("barcodeId") String barcodeId) {
        LOGGER.info("GET /api/v1/equipment/barcode/{}", barcodeId);
        return equipmentService.equipmentByBarcodeId(barcodeId);
    }

    /**
     * Deletes a specific piece of equipment by its unique ID.
     *
     * @param id the unique ID of the equipment to be deleted
     */
    @PermitAll
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEquipmentById(@PathVariable("id") Long id) {
        LOGGER.info("DELETE /api/v1/equipment/{}", id);
        equipmentService.deleteEquipment(id);
    }

    /**
     * Partially updates an existing equipment item.
     *
     * @param id the ID of the equipment to update
     * @param updateDto the DTO containing the fields to update
     * @return the updated equipment as a detail DTO
     */
    @PermitAll
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EquipmentDetailDto updateEquipment(
        @PathVariable("id") Long id,
        @Valid @RequestBody EquipmentUpdateDto updateDto
    ) {
        LOGGER.info("PATCH /api/v1/equipment/{} - Body: {}", id, updateDto);
        return equipmentService.updateEquipment(id, updateDto);
    }

    /**
     * Searches for equipment based on optional query parameters.
     * The parameters are passed in the URL (e.g., ?type=SKI&model=Atomic).
     *
     * @param searchDto dynamically mapped from URL query parameters
     * @return a list of equipment matching the criteria
     */
    @PermitAll
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EquipmentDetailDto> searchEquipment(EquipmentSearchDto searchDto) {
        LOGGER.info("GET /api/v1/equipment (Search) with parameters: {}", searchDto);
        return equipmentService.searchEquipment(searchDto);
    }

}
