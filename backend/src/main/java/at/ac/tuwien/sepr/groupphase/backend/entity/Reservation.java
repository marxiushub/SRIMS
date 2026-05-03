package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This Class represents a Reservation.
 * */
@Entity
@Table(name = "Reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<ReservationRelation> items = new ArrayList<>();

    private String customerName;

    private Date picUpTime;

    private int rentDurationDays;

    private Boolean confirmationEmailSent;

    protected Reservation() {}

    public Reservation(String customerName, Date picUpTime) {
        this.customerName = customerName;
        this.picUpTime = picUpTime;
    }

    /**
     * Setter.
     * */
    public void setItems(Equipment equipment, int quantity) {
        ReservationRelation item = new ReservationRelation(this, equipment, quantity);
        items.add(item);
    }

    public void setRentDurationDays(int days) {
        this.rentDurationDays = days;
    }

    public void setConfirmationEmailSent() {
        this.confirmationEmailSent = true;
    }

    /**
     * Getter.
     * */
    public List<ReservationRelation> getItems() {
        return items;
    }

    public int getRentDurationDays() {
        return rentDurationDays;
    }



}
