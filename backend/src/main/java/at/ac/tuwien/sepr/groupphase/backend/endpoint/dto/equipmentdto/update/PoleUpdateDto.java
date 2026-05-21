package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PoleUpdateDto extends EquipmentUpdateDto {
    @Max(value = 145, message = "Pole is too long")
    @Min(value = 70, message = "Pole is too short")


    private Double length;

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

}
