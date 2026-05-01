package at.ac.tuwien.sepr.groupphase.backend.entity.equipment;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.persistence.Entity;

@Entity
public class Ski extends Equipment {
    private double length;

    protected Ski() {
    }

    public Ski(String model, double price, double length, RentalStatus status, SkillLevel targetSkillLevel) {
        super(model, price, status, targetSkillLevel);
        this.length = length;
    }

    public double getLength() {
        return length;
    }
}
