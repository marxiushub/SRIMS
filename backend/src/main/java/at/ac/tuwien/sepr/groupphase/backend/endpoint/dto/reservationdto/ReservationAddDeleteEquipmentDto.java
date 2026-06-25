package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class ReservationAddDeleteEquipmentDto {

    @Positive(message = "Reservation id must be greater than 0")
    @NotNull(message = "id should not be null")
    private Long id;

    private List<Long> equipmentIds;

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
