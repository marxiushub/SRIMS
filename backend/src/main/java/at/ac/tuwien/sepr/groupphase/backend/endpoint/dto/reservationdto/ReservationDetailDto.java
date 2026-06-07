package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservationDetailDto {

    private Long id;
    private Long customerProfileId;
    private Long accountId;
    private String customerName;
    private LocalTime pickUpTime;
    private LocalDate startDate;
    private LocalDate endDate;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }

}