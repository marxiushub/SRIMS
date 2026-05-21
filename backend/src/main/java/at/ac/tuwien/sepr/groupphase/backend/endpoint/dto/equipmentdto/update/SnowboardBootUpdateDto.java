package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update;


public class SnowboardBootUpdateDto extends EquipmentUpdateDto {

    private String lancingSystem;

    public String getLacingSystem() {
        return lancingSystem;
    }

    public void setLacingSystem(String lancingSystem) {
        this.lancingSystem = lancingSystem;
    }

}
