package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * Dto for the creation of Helmets.
 * */

public class HelmetCreationDto extends EquipmentCreationDto {

    @Positive(message = "size is negative")
    @Max(value = 66, message = "helmet too large")
    @Min(value = 48, message = "helmet too small")
    private double size;

    private final EquipmentType type = EquipmentType.HELMET;

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    @Override
    public EquipmentType getType() {
        return type;
    }

    @Override
    public Equipment toEntity() {
        return new Helmet(getModel(), getPrice(), size, getStatus(), getTargetSkillLevel());
    }


}
