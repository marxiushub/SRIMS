package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail;

/**
 * Data Transfer Object (DTO) for representing the details of a ski boot equipment item.
 * Extends the base {@link EquipmentDetailDto} to include specific attributes related to ski boots.
 */
public class SkiBootDetailDto extends EquipmentDetailDto {
    private int soleLengthMm;

    public int getSoleLengthMm() {
        return soleLengthMm;
    }

    public void setSoleLengthMm(int soleLengthMm) {
        this.soleLengthMm = soleLengthMm;
    }
}
