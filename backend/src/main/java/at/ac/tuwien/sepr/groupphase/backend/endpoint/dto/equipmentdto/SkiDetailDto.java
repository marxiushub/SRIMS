package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

/**
 * Data Transfer Object (DTO) for representing the details of a ski equipment item.
 * Extends the base {@link EquipmentDetailDto} to include specific attributes related to skis.
 */
public class SkiDetailDto extends EquipmentDetailDto {
    private double length;

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }
}
