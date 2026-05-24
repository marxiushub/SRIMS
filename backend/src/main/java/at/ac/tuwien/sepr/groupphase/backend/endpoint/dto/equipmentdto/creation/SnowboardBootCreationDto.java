package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation;


import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SnowboardBoot;

public class SnowboardBootCreationDto extends EquipmentCreationDto {


    private String lacingSystem;

    public String getLacingSystem() {
        return lacingSystem;
    }

    public void setLacingSystem(String lacingSystem) {
        this.lacingSystem = lacingSystem;
    }

    private final EquipmentType type = EquipmentType.SNOWBOARDBOOT;

    @Override
    public EquipmentType getType() {
        return type;
    }

    @Override
    public Equipment toEntity() {
        return new SnowboardBoot(getModel(), getPrice(), lacingSystem, getStatus(), getTargetSkillLevel());
    }

}
