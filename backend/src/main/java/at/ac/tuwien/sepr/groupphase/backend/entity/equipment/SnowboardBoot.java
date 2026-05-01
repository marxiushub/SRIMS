package at.ac.tuwien.sepr.groupphase.backend.entity.equipment;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.persistence.Entity;

@Entity
public class SnowboardBoot extends Equipment {

    private String lancingSystem;

    protected SnowboardBoot() {
    }

    public SnowboardBoot(String model, double price, String lancingSystem, RentalStatus status, SkillLevel targetSkillLevel) {
        super(model, price, status, targetSkillLevel);
        this.lancingSystem = lancingSystem;
    }

    public String getLanzingSystem() {
        return lancingSystem;
    }
}
