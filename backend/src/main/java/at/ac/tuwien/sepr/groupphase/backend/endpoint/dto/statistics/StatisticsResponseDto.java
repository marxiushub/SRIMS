package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.statistics;

import java.util.Map;

public class StatisticsResponseDto {

    private Map<String, Integer> modelCounts;

    private Map<Long, Integer> itemCounts;

    private boolean detailDegree;

    public StatisticsResponseDto() {
    }

    public boolean getDetailDegree() {
        return detailDegree;
    }

    public void setDetailDegree(boolean detailDegree) {
        this.detailDegree = detailDegree;
    }

    public Map<String, Integer> getModelCounts() {
        return modelCounts;
    }

    public void setModelCounts(Map<String, Integer> modelCounts) {
        this.modelCounts = modelCounts;
    }

    public Map<Long, Integer> getItemCounts() {
        return itemCounts;
    }

    public void setItemCounts(Map<Long, Integer> itemCounts) {
        this.itemCounts = itemCounts;
    }

}
