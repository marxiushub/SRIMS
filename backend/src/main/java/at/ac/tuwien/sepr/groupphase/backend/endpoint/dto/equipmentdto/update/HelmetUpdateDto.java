package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class HelmetUpdateDto extends EquipmentUpdateDto {

    @Max(value = 66, message = "helmet too large")
    @Min(value = 48, message = "helmet too small")
    private Double size;

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

}
