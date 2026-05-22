package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservationCreationDto {

    @NotNull(message = "Customer must be specified")
    private Long customerProfileId;

    @NotEmpty(message = "At least one equipment item must be selected")
    private List<Long> equipmentIds;

    @NotNull(message = "Pickup time must not be empty")
    private LocalTime pickUpTime;

    @NotNull(message = "Pickup date must not be empty")
    @FutureOrPresent(message = "Pickup date must not be in the past")
    private LocalDate pickUpDate;

    @Min(value = 1, message = "Rental must last at least 1 day")
    private int rentDurationDays;

    public ReservationCreationDto() {}

    public Long getCustomerProfileId() { return customerProfileId; }
    public void setCustomerProfileId(Long customerProfileId) { this.customerProfileId = customerProfileId; }

    public List<Long> getEquipmentIds() { return equipmentIds; }
    public void setEquipmentIds(List<Long> equipmentIds) { this.equipmentIds = equipmentIds; }

    public LocalTime getPickUpTime() { return pickUpTime; }
    public void setPickUpTime(LocalTime pickUpTime) { this.pickUpTime = pickUpTime; }

    public LocalDate getPickUpDate() { return pickUpDate; }
    public void setPickUpDate(LocalDate pickUpDate) { this.pickUpDate = pickUpDate; }

    public int getRentDurationDays() { return rentDurationDays; }
    public void setRentDurationDays(int rentDurationDays) { this.rentDurationDays = rentDurationDays; }
}