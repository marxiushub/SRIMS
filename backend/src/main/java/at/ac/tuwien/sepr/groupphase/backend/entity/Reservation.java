package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
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


    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationRelation> items = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "customer_profile_id", nullable = false)
    private CustomerProfile customerProfile;

    private LocalTime pickUpTime;

    private LocalDate pickUpDate;

    private int rentDurationDays;

    private Boolean confirmationEmailSent;

    protected Reservation() {}

    public Reservation(CustomerProfile customerProfile , LocalTime pickUpTime,LocalDate pickUpDate, int rentDurationDays) {
        this.customerProfile = customerProfile;
        this.pickUpTime = pickUpTime;
        this.pickUpDate = pickUpDate;
        this.rentDurationDays = rentDurationDays;
    }

    @Transient
    public LocalDate getReturnDate() {
        if (this.pickUpDate == null) {
            return null;
        }
        return this.pickUpDate.plusDays(this.rentDurationDays);
    }

    /**
     * Setter.
     * */
    public void addItem(Equipment equipment) {
        ReservationRelation item = new ReservationRelation(this, equipment);
        items.add(item);
    }

    public void setRentDurationDays(int days) {
        this.rentDurationDays = days;
    }

    public void setConfirmationEmailSent() {
        this.confirmationEmailSent = true;
    }

    public void setPickUpDate(LocalDate pickUpDate) {
        this.pickUpDate = pickUpDate;
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

    public Boolean getConfirmationEmailSent() {
        return confirmationEmailSent;
    }

    public LocalTime getPickUpTime() {
        return pickUpTime;
    }
    public CustomerProfile getCustomerProfile() {
        return customerProfile;
    }

    public Long getId() {
        return id;
    }

}
