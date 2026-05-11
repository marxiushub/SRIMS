package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

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

}
