package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/**
 * Dto for creation of Skis.
 * */

public class SkiCreationDto extends EquipmentCreationDto {

    @Positive(message = "length is negative")
    @Max(value = 210, message = "Ski is too long")
    @Min(value = 70, message = "Ski is too short")
    private double length;

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }



}
