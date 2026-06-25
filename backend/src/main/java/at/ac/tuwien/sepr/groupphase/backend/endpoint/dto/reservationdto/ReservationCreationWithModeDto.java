package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Same fields as ReservationCreationDto, plus a "mode" field. Used as the request body for
 * the barcode-scanner's walk-in checkout endpoint (POST /api/v1/scanner), so the backend can
 * tell a normal rental walk-in apart from a maintenance walk-in.
 * BarcodeScannerServiceImpl extracts "mode" and converts the rest into a plain
 * ReservationCreationDto.
 **/
public class ReservationCreationWithModeDto {

    @Positive(message = "customerProfileId must be greater than 0")
    @NotNull(message = "Customer must be specified")
    private Long customerProfileId;

    @NotEmpty(message = "At least one equipment item must be selected")
    @Size(min = 1, max = 100, message = "You must select between 1 and 100 equipment items")
    private List<Long> equipmentIds;

    @NotNull(message = "Pickup time must not be empty")
    private LocalTime pickUpTime;

    @NotNull(message = "Pickup date must not be empty")
    @FutureOrPresent(message = "Pickup date must not be in the past")
    private LocalDate startDate;

    @NotNull(message = "Return date must not be empty")
    @FutureOrPresent(message = "Return date must not be in the past")
    private LocalDate endDate;

    @NotNull(message = "Reservation Status must be specified")
    private at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus reservationStatus;

    /**
     * Either "RENTAL" (normal walk-in checkout) or "MAINTENANCE" (send equipment into
     * maintenance).
     */
    @NotNull(message = "Mode must be specified")
    private String mode;

    public ReservationCreationWithModeDto() {
    }

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

    public at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Converts this DTO into a plain ReservationCreationDto (without "mode"), which is what
     * ReservationService.createReservation actually expects.
     */
    public ReservationCreationDto toReservationCreationDto() {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setCustomerProfileId(this.customerProfileId);
        dto.setEquipmentIds(this.equipmentIds);
        dto.setPickUpTime(this.pickUpTime);
        dto.setStartDate(this.startDate);
        dto.setEndDate(this.endDate);
        dto.setReservationStatus(this.reservationStatus);
        return dto;
    }
}