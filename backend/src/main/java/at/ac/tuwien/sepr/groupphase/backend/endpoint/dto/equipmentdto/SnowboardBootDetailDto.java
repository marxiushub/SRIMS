package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

/**
 * Data Transfer Object (DTO) for representing the details of a snowboard boot equipment item.
 * Extends the base {@link EquipmentDetailDto} to include specific attributes related to snowboard boots.
 */
public class SnowboardBootDetailDto extends EquipmentDetailDto {
    private String lacingSystem;

    public String getLacingSystem() {
        return lacingSystem;
    }

    public void setLacingSystem(String lacingSystem) {
        this.lacingSystem = lacingSystem;
    }
}
