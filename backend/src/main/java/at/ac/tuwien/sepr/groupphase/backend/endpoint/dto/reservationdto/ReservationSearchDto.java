package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto;

import at.ac.tuwien.sepr.groupphase.backend.entity.TimePeriods;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationSearchDto {
    private Long customerProfileId;
    private Long accountId;
    private LocalDate pickUpDate;
    private LocalTime pickUpTime;
    private TimePeriods timePeriod;

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

    public LocalDate getPickUpDate() {
        return pickUpDate;
    }

    public void setPickUpDate(LocalDate pickUpDate) {
        this.pickUpDate = pickUpDate;
    }

    public LocalTime getPickUpTime() {
        return pickUpTime;
    }

    public void setPickUpTime(LocalTime pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    public TimePeriods getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriods timePeriod) {
        this.timePeriod = timePeriod;
    }
}
