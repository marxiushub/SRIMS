package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.overview;

import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;

import java.util.Map;

/**
 * DTO representing an aggregated overview of equipment counts, grouped by equipment type and rental status.
 */
public class EquipmentStatusOverviewDto {

    private Map<EquipmentType, Map<RentalStatus, Long>> counts;

    public EquipmentStatusOverviewDto() {
    }

    public EquipmentStatusOverviewDto(Map<EquipmentType, Map<RentalStatus, Long>> counts) {
        this.counts = counts;
    }

    public Map<EquipmentType, Map<RentalStatus, Long>> getCounts() {
        return counts;
    }

    public void setCounts(Map<EquipmentType, Map<RentalStatus, Long>> counts) {
        this.counts = counts;
    }
}
