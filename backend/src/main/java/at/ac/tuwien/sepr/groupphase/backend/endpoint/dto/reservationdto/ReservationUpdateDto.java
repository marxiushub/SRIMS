package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservationUpdateDto {

    @NotNull(message = "id should not be null")
    private Long id;

    private LocalTime pickUpTime;

    private LocalDate pickUpDate;

    private int rentDurationDays;

    private List<Long> equipmentIds;


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

    public Integer getRentDurationDays() {
        return rentDurationDays;
    }

    public void setRentDurationDays(Integer rentDurationDays) {
        this.rentDurationDays = rentDurationDays;
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
}
