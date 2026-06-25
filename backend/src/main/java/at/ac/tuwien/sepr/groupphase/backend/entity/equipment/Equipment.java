package at.ac.tuwien.sepr.groupphase.backend.entity.equipment;


import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.TimePeriods;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import jakarta.persistence.CascadeType;
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
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * This Entity represents a generic piece of equipment.
 *
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Equipment {
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    private RentalStatus status;

    @Column(unique = true)
    private String barcodeId;

    @PrePersist
    public void generateBarcode() {
        if (barcodeId == null) {
            barcodeId = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, ALPHABET, 10);
        }
    }

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimePeriods> timePeriodsList = new ArrayList<>();

    private int usageDurationDays;

    @Enumerated(EnumType.STRING)
    private SkillLevel targetSkillLevel;

    protected Equipment() {
    }

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

    public void addTimePeriod(LocalDate start, LocalDate end, PeriodType periodType, Reservation reservation) {
        TimePeriods period = new TimePeriods(this, start, end, periodType, reservation);
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

    public String getEquipmentTypeName() {
        return this.getClass().getSimpleName();
    }
}
