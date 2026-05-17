package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class SnowboardUpdateDto extends EquipmentUpdateDto {

    @Max(value = 180, message = "Snowboard is too long")
    @Min(value = 80, message = "Snowboard is too short")
    private Double length;

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    private final EquipmentType type = EquipmentType.SNOWBOARD;
}
