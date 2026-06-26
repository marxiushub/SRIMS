package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.statistics;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import jakarta.validation.constraints.NotNull;
import org.aspectj.lang.annotation.Before;

import java.time.LocalDate;

public class StatisticsRequestDto {

    public StatisticsRequestDto() {
    }

    @NotNull(message = "Start time must not be empty")
    private LocalDate searchStart;

    @NotNull(message = "End time must not be empty")
    private LocalDate searchEnd;

    /**
     * if = true the statistic describes individual items, if = 0 only the frequency of models is described.
     * */
    private boolean detailDegree;

    /**
     * if null the statistic of all the equip is requested.
     * */
    private EquipmentType type;

    public boolean getDetailDegree() {
        return detailDegree;
    }

    public void setDetailDegree(boolean detailDegree) {
        this.detailDegree = detailDegree;
    }


    public LocalDate getSearchStart() {
        return searchStart;
    }

    public void setSearchStart(LocalDate searchStart) {
        this.searchStart = searchStart;
    }


    public LocalDate getSearchEnd() {
        return searchEnd;
    }

    public void setSearchEnd(LocalDate searchEnd) {
        this.searchEnd = searchEnd;
    }


    public EquipmentType getType() {
        return type;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

}
