package at.ac.tuwien.sepr.groupphase.backend.entity.equipment;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.persistence.Entity;

@Entity
public class Helmet extends Equipment {
    private double size;

    protected Helmet() {
    }

    public Helmet(String model, double price, double size, RentalStatus status, SkillLevel targetSkillLevel) {
        super(model, price, status, targetSkillLevel);
        this.size = size;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.HELMET; // Replace with your actual Enum value
    }
}
