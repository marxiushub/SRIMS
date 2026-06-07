package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto;

import jakarta.validation.constraints.FutureOrPresent;
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
    private LocalDate startDate;

    @NotNull(message = "Return date must not be empty")
    @FutureOrPresent(message = "Return date must not be in the past")
    private LocalDate endDate;

    public ReservationCreationDto() {}

    public Long getCustomerProfileId() {
        return customerProfileId;
    }

    public void setCustomerProfileId(Long customerProfileId) {
        this.customerProfileId = customerProfileId;
    }

    public List<Long> getEquipmentIds() {
        return equipmentIds;
    }

    public void setEquipmentIds(List<Long> equipmentIds) {
        this.equipmentIds = equipmentIds;
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

}