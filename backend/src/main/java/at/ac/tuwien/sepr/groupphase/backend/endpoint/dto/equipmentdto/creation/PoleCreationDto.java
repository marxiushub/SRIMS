package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Pole;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public class PoleCreationDto extends EquipmentCreationDto {

    @Positive(message = "length is negative")
    @Max(value = 145, message = "Pole is too long")
    @Min(value = 70, message = "Pole is too short")
    private double length;

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    private final EquipmentType type = EquipmentType.POLE;

    @Override
    public EquipmentType getType() {
        return type;
    }

    @Override
    public Equipment toEntity() {
        return new Pole(getModel(), getPrice(), length, getStatus(), getTargetSkillLevel());
    }
}
