package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;

public class SnowboardBootDetailDto extends EquipmentDetailDto {
    private String lancingSystem;

    public String getLancingSystem() {
        return lancingSystem;
    }

    public void setLancingSystem(String lancingSystem) {
        this.lancingSystem = lancingSystem;
    }
}
