package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Id;


@Entity
public class ReservationRelation {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
     private Equipment equipment;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
     private Reservation reservation;

    protected ReservationRelation() {}

    public ReservationRelation(Reservation reservation, Equipment equipment) {
        this.equipment = equipment;
        this.reservation = reservation;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public Long getId() {
        return id;
    }

}
