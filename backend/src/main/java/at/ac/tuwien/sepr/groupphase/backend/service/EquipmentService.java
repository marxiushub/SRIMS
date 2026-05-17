package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;

import java.util.List;

/**
 * Service interface for managing equipment-related operations.
 */
public interface EquipmentService {

    /**
     * Creates a new entry of type equipment in the Database.
     *
     * @return an Equipment entity
     * */
    public Equipment createEquipment(EquipmentCreationDto dto);

    /**
     * Retrieves a list of all equipment available in the system.
     *
     * @return a list of {@link EquipmentDetailDto} representing the equipment information
     */
    public List<EquipmentDetailDto> allEquipment();

    /**
     * Retrieves a list of equipment filtered by the specified type.
     *
     * @param type the type of equipment to filter by (e.g., "helmet", "ski", "snowboard")
     * @return a list of {@link EquipmentDetailDto} representing the equipment information for the specified type
     */
    public List<EquipmentDetailDto> equipmentByType(String type);
}
