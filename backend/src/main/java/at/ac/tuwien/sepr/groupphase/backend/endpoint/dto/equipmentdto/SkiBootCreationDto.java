package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SkiBoot;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * Dto for the creation of a Skiboot.
 * */

public class SkiBootCreationDto extends EquipmentCreationDto {

    @Positive(message = "SkiBoot has negative size")
    @Min(value = 140, message = "SkiBoot is too large")
    @Max(value = 350, message = "SkiBoot is too small")
    private int soleLengthMm;

    public double getSoleLengthMm() {
        return soleLengthMm;
    }

    public void setSoleLengthMm(int soleLengthMm) {
        this.soleLengthMm = soleLengthMm;
    }

    private final EquipmentType type = EquipmentType.SKIBOOT;

    @Override
    public EquipmentType getType() {
        return type;
    }

    @Override
    public Equipment toEntity() {
        return new SkiBoot(getModel(), getPrice(), soleLengthMm, getStatus(), getTargetSkillLevel());
    }
}
