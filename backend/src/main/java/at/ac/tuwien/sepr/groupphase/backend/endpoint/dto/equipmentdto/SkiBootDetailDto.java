package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

public class SkiBootDetailDto extends EquipmentDetailDto {
    private int soleLengthMm;

    public int getSoleLengthMm() {
        return soleLengthMm;
    }

    public void setSoleLengthMm(int soleLengthMm) {
        this.soleLengthMm = soleLengthMm;
    }
}
