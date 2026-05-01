package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Id;


@Entity
public class ReservationRelation {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
     private Equipment equipment;

    @ManyToOne
     private Reservation reservation;

    private int quantity;

    protected ReservationRelation() {}

    public ReservationRelation(Reservation reservation, Equipment equipment, int quantity) {
        this.equipment = equipment;
        this.reservation = reservation;
        this.quantity = quantity;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public int getQuantity() {
        return quantity;
    }

}
