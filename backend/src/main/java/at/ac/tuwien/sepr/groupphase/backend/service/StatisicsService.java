package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.statistics.StatisticsRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.statistics.StatisticsResponseDto;

public interface StatisicsService {

    /**
     * Retrieves rental statistics aggregated by equipment model based on the provided filters.
     * Counts how many days each equipment model  or item (e.g., "Atomic Redster X9") was rented
     * within the specified time frame and equipment type.
     *
     * @param request the request DTO containing the date range and optional equipment type filter
     * @return a {@link StatisticsResponseDto} containing a map of model names to their rental counts
     */
    public StatisticsResponseDto getEquipmentStatistics(StatisticsRequestDto request);




}
