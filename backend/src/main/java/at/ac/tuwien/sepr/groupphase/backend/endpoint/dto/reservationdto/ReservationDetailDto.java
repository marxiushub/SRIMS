package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservationDetailDto {

    private Long id;
    private Long customerProfileId;
    private String customerName;
    private LocalTime pickUpTime;
    private LocalDate pickUpDate;
    private LocalDate returnDate;
    private int rentDurationDays;
    private Boolean confirmationEmailSent;

    private List<EquipmentDetailDto> items;

    public ReservationDetailDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getCustomerProfileId() {
        return customerProfileId;
    }

    public void setCustomerProfileId(Long customerProfileId) {
        this.customerProfileId = customerProfileId;
    }

    public LocalTime getPickUpTime() {
        return pickUpTime;
    }

    public void setPickUpTime(LocalTime pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    public LocalDate getPickUpDate() {
        return pickUpDate;
    }

    public void setPickUpDate(LocalDate pickUpDate) {
        this.pickUpDate = pickUpDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public int getRentDurationDays() {
        return rentDurationDays;
    }

    public void setRentDurationDays(int rentDurationDays) {
        this.rentDurationDays = rentDurationDays;
    }

    public Boolean getConfirmationEmailSent() {
        return confirmationEmailSent;
    }

    public void setConfirmationEmailSent(Boolean confirmationEmailSent) {
        this.confirmationEmailSent = confirmationEmailSent;
    }

    public List<EquipmentDetailDto> getItems() {
        return items;
    }

    public void setItems(List<EquipmentDetailDto> items) {
        this.items = items;
    }

}