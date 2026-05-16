package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

/**
 * Data Transfer Object (DTO) for representing the details of a pole equipment item.
 * Extends the base {@link EquipmentDetailDto} to include specific attributes related to poles.
 */
public class PoleDetailDto extends EquipmentDetailDto {
    private double length;

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }
}
