package at.ac.tuwien.sepr.groupphase.backend.entity.equipment;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.persistence.Entity;

@Entity
public class SnowboardBoot extends Equipment {

    private String lacingSystem;

    protected SnowboardBoot() {
    }

    public SnowboardBoot(String model, double price, String lacingSystem, RentalStatus status, SkillLevel targetSkillLevel) {
        super(model, price, status, targetSkillLevel);
        this.lacingSystem = lacingSystem;
    }

    public String getLacingSystem() {
        return lacingSystem;
    }

    public void setLacingSystem(String lacingSystem) {
        this.lacingSystem = lacingSystem;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.SNOWBOARDBOOT;
    }
}
