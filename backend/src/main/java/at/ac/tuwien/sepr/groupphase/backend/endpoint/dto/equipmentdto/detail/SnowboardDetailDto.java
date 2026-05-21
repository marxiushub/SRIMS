package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail;

/**
 * Data Transfer Object (DTO) for representing the details of a snowboard equipment item.
 * Extends the base {@link EquipmentDetailDto} to include specific attributes related to snowboards.
 */
public class SnowboardDetailDto extends EquipmentDetailDto {
    private double length;

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }
}
