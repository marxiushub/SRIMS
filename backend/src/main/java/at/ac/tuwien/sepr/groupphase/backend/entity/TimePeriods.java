package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import jakarta.persistence.GenerationType;

import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import jakarta.persistence.ManyToOne;
import java.util.Date;

@Entity
public class TimePeriods {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date startDate;

    @Column(nullable = false)
    private Date endDate;

    @ManyToOne
    private Equipment equipment;

    protected TimePeriods() {}

    public TimePeriods(Equipment equipment, Date startDate, Date endDate, PeriodType periodType) {
        this.endDate = endDate;
        this.equipment = equipment;
        this.startDate = startDate;
        this.periodType = periodType;
    }

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;
}
