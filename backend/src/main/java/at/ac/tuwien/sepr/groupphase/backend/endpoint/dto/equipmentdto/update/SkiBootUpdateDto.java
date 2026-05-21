package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class SkiBootUpdateDto extends EquipmentUpdateDto {

    @Min(value = 140, message = "SkiBoot is too large")
    @Max(value = 350, message = "SkiBoot is too small")
    private Double soleLengthMm;

    public Double getSoleLengthMm() {
        return soleLengthMm;
    }

    public void setSoleLengthMm(Double soleLengthMm) {
        this.soleLengthMm = soleLengthMm;
    }
}
