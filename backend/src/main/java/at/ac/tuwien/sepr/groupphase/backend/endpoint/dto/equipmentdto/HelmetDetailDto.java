package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

/**
 * Data Transfer Object (DTO) for representing the details of a helmet equipment item.
 * Extends the base {@link EquipmentDetailDto} to include specific attributes related to helmets.
 */
public class HelmetDetailDto extends EquipmentDetailDto {
    private double size;

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }
}
