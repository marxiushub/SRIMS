package at.ac.tuwien.sepr.groupphase.backend.entity.equipment;

import at.ac.tuwien.sepr.groupphase.backend.entity.TimePeriods;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.ArrayList;

/**
 * This Entity represents a generic piece of equipment.
 *
 */

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

    @Column(nullable = true, unique = true)
    private String barcodeId;

    //Mocked generating of barcodeIds
    @PrePersist
    public void generateBarcode() {
        if (barcodeId == null) {
            barcodeId = UUID.randomUUID().toString();
        }
    }

    @OneToMany(mappedBy = "equipment", orphanRemoval = true)
    private List<TimePeriods> timePeriodsList = new ArrayList<>();

    private int usageDurationDays;

    @Enumerated(EnumType.STRING)
    private SkillLevel targetSkillLevel;

    protected Equipment() {
    }

    //für wartungsdaten: 1:x beziehung mit eigener tabelle
    public Equipment(String model, double price, RentalStatus status, SkillLevel targetSkillLevel) {
        this.model = model;
        this.price = price;
        this.status = status;
        usageDurationDays = 0;
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

    public SkillLevel getTargetSkillLevel() {
        return targetSkillLevel;
    }

    public String getBarcodeId() {
        return barcodeId;
    }

    /**
     * Setter.
     *
     */
    public void updateStatus(RentalStatus newStatus) {
        this.status = newStatus;
    }

    public void incrementUsageDurationDays() {
        this.usageDurationDays++;
    }

    public void resetUsageDurationDays() {
        this.usageDurationDays = 0;
    }

    public void addTimePeriod(LocalDate start, LocalDate end, PeriodType periodType) {
        TimePeriods period = new TimePeriods(this, start, end, periodType);
        timePeriodsList.add(period);
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setStatus(RentalStatus status) {
        this.status = status;
    }

    public void setTargetSkillLevel(SkillLevel skillLevel) {
        this.targetSkillLevel = skillLevel;
    }


    public abstract EquipmentType getEquipmentType();
}
