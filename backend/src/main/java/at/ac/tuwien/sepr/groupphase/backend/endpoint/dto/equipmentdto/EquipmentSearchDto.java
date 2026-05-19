package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;

/**
 * Data Transfer Object for searching and filtering equipment.
 * All fields are optional (nullable) to allow partial search queries.
 */
public class EquipmentSearchDto {

    private String model;
    private EquipmentType type;
    private RentalStatus status;
    private SkillLevel targetSkillLevel;

    // --- Getter & Setter ---

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public EquipmentType getType() {
        return type;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public RentalStatus getStatus() {
        return status;
    }

    public void setStatus(RentalStatus status) {
        this.status = status;
    }

    public SkillLevel getTargetSkillLevel() {
        return targetSkillLevel;
    }

    public void setTargetSkillLevel(SkillLevel targetSkillLevel) {
        this.targetSkillLevel = targetSkillLevel;
    }
}