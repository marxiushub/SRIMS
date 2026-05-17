package at.ac.tuwien.sepr.groupphase.backend.entity.equipment;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.persistence.Entity;

@Entity
public class Snowboard extends Equipment {
    private double length;

    protected Snowboard() {
    }

    public Snowboard(String model, double price, double length, RentalStatus status, SkillLevel targetSkillLevel) {
        super(model, price, status, targetSkillLevel);
        this.length = length;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.SNOWBOARD;
    }
}
