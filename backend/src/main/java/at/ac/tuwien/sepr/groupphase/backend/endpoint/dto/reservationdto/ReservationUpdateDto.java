package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservationUpdateDto {

    @NotNull(message = "id should not be null")
    private Long id;

    private LocalTime pickUpTime;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<Long> equipmentIds;

    private Long customerProfileId;

    private ReservationStatus reservationStatus;

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

    public List<Long> getEquipmentIds() {
        return equipmentIds;
    }

    public void setEquipmentIds(List<Long> equipmentIds) {
        this.equipmentIds = equipmentIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}
