package at.ac.tuwien.sepr.groupphase.backend.entity.equipment;

import at.ac.tuwien.sepr.groupphase.backend.entity.TimePeriods;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.util.Date;
import java.util.List;

/**
 * This Entity represents a generic piece of equipment.
 *
 */

//PS: Dominik: Ich würde für die Vererbung Joined subclasses verwenden, wobei jede art von equipment
//seine eigene tabelle hat. ist gut für erweiterbarkeit(müssen keine bestehenden tabellen ändern
//wenn was dazu kommt und ist am intuitivsten, wir können das aber natürlich noch ändern
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    private RentalStatus status;

    @Column(nullable = false, unique = true)
    private String barcodeId;

    @OneToMany(mappedBy = "equipment")
    private List<TimePeriods> timePeriodsList;

    private int unsageDurationDays;

    private SkillLevel targetSkillLevel;

    protected Equipment() {
    }

    //für wartungsdaten: 1:x beziehung mit eigener tabelle
    public Equipment(String model, double price, RentalStatus status, SkillLevel targetSkillLevel) {
        this.model = model;
        this.price = price;
        this.status = status;
        unsageDurationDays = 0;
        this.targetSkillLevel = targetSkillLevel;
    }

    /**
     * getter.
     *
     */
    public Long getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public String getModel() {
        return model;
    }

    public RentalStatus getStatus() {
        return status;
    }

    public List<TimePeriods> getTimePeriodsList() {
        return timePeriodsList;
    }

    /**
     * Setter.
     *
     */
    public void updateStatus(RentalStatus newStatus) {
        this.status = newStatus;
    }

    public void incrementUsageDurationDays() {
        this.unsageDurationDays++;
    }

    public void resetUsageDurationDays() {
        this.unsageDurationDays = 0;
    }

    public void addTimePeriod(Date start, Date end, PeriodType periodType) {
        TimePeriods period = new TimePeriods(this, start, end, periodType);
        timePeriodsList.add(period);
    }
}
