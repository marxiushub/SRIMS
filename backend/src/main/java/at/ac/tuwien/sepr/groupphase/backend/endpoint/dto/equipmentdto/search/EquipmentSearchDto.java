package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.search;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Data Transfer Object for searching and filtering equipment.
 * All fields are optional (nullable) to allow partial search queries.
 */
public class EquipmentSearchDto {

    @Size(max = 100, message = "model must be max 100 characters")
    private String model;

    private EquipmentType type;
    private RentalStatus status;
    private SkillLevel targetSkillLevel;
    private LocalDate start;
    private LocalDate end;


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

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public LocalDate getEnd() {
        return end;
    }
}