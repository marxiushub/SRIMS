package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
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

    @Column(nullable = false)
    private double totalPrice;

    private LocalTime pickUpTime;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean confirmationEmailSent;

    protected Reservation() {
    }

    public Reservation(CustomerProfile customerProfile, LocalTime pickUpTime, LocalDate startDate, LocalDate endDate) {
        this.customerProfile = customerProfile;
        this.pickUpTime = pickUpTime;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Setter.
     *
     */
    public void addItem(Equipment equipment) {
        ReservationRelation item = new ReservationRelation(this, equipment);
        items.add(item);
    }


    public void setConfirmationEmailSent() {
        this.confirmationEmailSent = true;
    }

    public void setPickUpTime(LocalTime pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    public void setCustomerProfile(CustomerProfile customerProfile) {
        this.customerProfile = customerProfile;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * Getter.
     *
     */
    public double getTotalPrice() {
        return totalPrice;
    }

    public List<ReservationRelation> getItems() {
        return items;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

}
