package at.ac.tuwien.sepr.groupphase.backend.entity.equipment;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.persistence.Entity;

@Entity
public class SkiBoot extends Equipment {
    private int soleLengthMm;

    protected SkiBoot() {
    }

    public SkiBoot(String model, double price, int soleLengthMm, RentalStatus status, SkillLevel targetSkillLevel) {
        super(model, price, status, targetSkillLevel);
        this.soleLengthMm = soleLengthMm;
    }

    public double getLength() {
        return soleLengthMm;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.SKIBOOT;
    }
}
