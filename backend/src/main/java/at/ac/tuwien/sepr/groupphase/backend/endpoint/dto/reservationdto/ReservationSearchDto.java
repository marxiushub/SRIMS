package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservationSearchDto {
    private Long customerProfileId;
    private Long accountId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime pickUpTime;
    private LocalDate searchRangeStart;
    private LocalDate searchRangeEnd;
    private List<Long> equipmentIds;
    private ReservationStatus reservationStatus;

    public Long getCustomerProfileId() {
        return customerProfileId;
    }

    public void setCustomerProfileId(Long customerProfileId) {
        this.customerProfileId = customerProfileId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public LocalTime getPickUpTime() {
        return pickUpTime;
    }

    public void setPickUpTime(LocalTime pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    public LocalDate getSearchRangeStart() {
        return searchRangeStart;
    }

    public void setSearchRangeStart(LocalDate searchRangeStart) {
        this.searchRangeStart = searchRangeStart;
    }

    public LocalDate getSearchRangeEnd() {
        return searchRangeEnd;
    }

    public void setSearchRangeEnd(LocalDate searchRangeEnd) {
        this.searchRangeEnd = searchRangeEnd;
    }

    public List<Long> getEquipmentIds() {
        return equipmentIds;
    }

    public void setEquipmentIds(List<Long> equipmentIds) {
        this.equipmentIds = equipmentIds;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}
