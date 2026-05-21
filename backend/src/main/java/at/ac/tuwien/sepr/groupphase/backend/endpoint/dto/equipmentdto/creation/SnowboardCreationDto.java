package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Snowboard;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * Dto for teh creation von Snowboards.
 * */

public class SnowboardCreationDto extends EquipmentCreationDto {

    @Positive(message = "length is negative")
    @Max(value = 180, message = "Snowboard is too long")
    @Min(value = 80, message = "Snowboard is too short")
    private double length;

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    private final EquipmentType type = EquipmentType.SNOWBOARD;

    @Override
    public EquipmentType getType() {
        return type;
    }

    @Override
    public Equipment toEntity() {
        return new Snowboard(getModel(), getPrice(), length, getStatus(), getTargetSkillLevel());
    }
}
