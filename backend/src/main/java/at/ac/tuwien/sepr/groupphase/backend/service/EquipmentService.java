package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;

import java.util.List;

/**
 * Service interface for managing equipment-related operations.
 */
public interface EquipmentService {


    /**
     * Retrieves a list of all equipment available in the system.
     *
     * @return a list of {@link EquipmentDetailDto} representing the equipment information
     */
    List<EquipmentDetailDto> allEquipment();

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
    Equipment createEquipment(EquipmentCreationDto dto);

    /**
     * Deletes an equipment entry from the system based on the specified type and ID.
     *
     * @param id the unique identifier of the equipment to delete
     */
    void deleteEquipment(Long id);

    @org.springframework.transaction.annotation.Transactional
    EquipmentDetailDto updateEquipment(Long id, EquipmentUpdateDto updateDto);
}
