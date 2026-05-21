package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail;

/**
 * Data Transfer Object (DTO) for representing the details of a snowboard boot equipment item.
 * Extends the base {@link EquipmentDetailDto} to include specific attributes related to snowboard boots.
 */
public class SnowboardBootDetailDto extends EquipmentDetailDto {
    private String lancingSystem;

    public String getLancingSystem() {
        return lancingSystem;
    }

    public void setLancingSystem(String lancingSystem) {
        this.lancingSystem = lancingSystem;
    }
}
