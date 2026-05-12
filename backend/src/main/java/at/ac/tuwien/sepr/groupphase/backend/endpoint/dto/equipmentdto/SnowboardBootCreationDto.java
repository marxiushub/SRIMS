package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto;


import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SnowboardBoot;

public class SnowboardBootCreationDto extends EquipmentCreationDto {


    private String lancingSystem;

    public String getLancingSystem() {
        return lancingSystem;
    }

    public void setLancingSystem(String lancingSystem) {
        this.lancingSystem = lancingSystem;
    }

    private final EquipmentType type = EquipmentType.SNOWBOARDBOOT;

    @Override
    public EquipmentType getType() {
        return type;
    }

    @Override
    public Equipment toEntity() {
        return new SnowboardBoot(getModel(), getPrice(), lancingSystem, getStatus(), getTargetSkillLevel());
    }

}
