package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;

import java.util.List;

/**
 * Service interface for managing equipment-related operations.
 */
public interface EquipmentService {

    /**
     * Retrieves a list of equipment filtered by the specified type.
     *
     * @param type the type of equipment to filter by (e.g., "helmet", "ski", "snowboard")
     * @return a list of {@link EquipmentDetailDto} representing the equipment information for the specified type
     */
    List<EquipmentDetailDto> equipmentByType(String type);

    /**
     * Retrieves detailed information about a specific equipment item based on its unique identifier.
     *
     * @param id the unique identifier of the equipment to retrieve
     * @return an {@link EquipmentDetailDto} containing the detailed information of the specified equipment
     */
    EquipmentDetailDto equipmentById(Long id);

    /**
     * Creates a new equipment entry in the system based on the provided creation data.
     *
     * @param dto the data transfer object containing the information needed to create the equipment
     * @return the created {@link Equipment} entity
     */
    List<Equipment> createEquipment(EquipmentCreationDto dto);

    /**
     * Deletes an equipment entry from the system based on the specified type and ID.
     *
     * @param id the unique identifier of the equipment to delete
     */
    void deleteEquipment(Long id);

    /**
     * Partially updates an existing piece of equipment.
     * Only the non-null fields provided in the {@code updateDto} will be applied to the existing entity.
     *
     * @param id the unique identifier of the equipment to update
     * @param updateDto the data transfer object containing the new values
     * @return an {@link EquipmentDetailDto} representing the updated equipment
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException if no equipment with the given ID exists in the database
     * @throws IllegalArgumentException if the type specified in the {@code updateDto} does not match the actual type of the existing equipment
     */
    EquipmentDetailDto updateEquipment(Long id, EquipmentUpdateDto updateDto);

    /**
     * Searches for equipment based on dynamic criteria.
     * All properties within the {@code searchDto} are optional. If a property is {@code null},
     * it will be ignored during the search process. If the entire DTO is {@code null} or empty,
     * all available equipment will be returned.
     *
     * @param searchDto the data transfer object containing the optional filter parameters
     * @return a list of {@link EquipmentDetailDto} matching the given criteria; an empty list if no matches are found
     */
    List<EquipmentDetailDto> searchEquipment(EquipmentSearchDto searchDto);
}
