package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class SkiUpdateDto extends EquipmentUpdateDto {
    @Max(value = 210, message = "Ski is too long")
    @Min(value = 70, message = "Ski is too short")
    private Double length;

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }
}
