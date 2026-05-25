package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.occupancy;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;

import java.time.LocalDate;

public class TimePeriodDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private PeriodType periodType;

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

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }
}
