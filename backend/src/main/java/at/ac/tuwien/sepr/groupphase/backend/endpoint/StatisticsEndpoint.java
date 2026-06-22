package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.statistics.StatisticsRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.statistics.StatisticsResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.StatisticsServiceImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.lang.invoke.MethodHandles;

/**
 * Represents the REST API endpoint for collecting information used for statistics.
 * */

@RestController
@RequestMapping("api/v1/statistics")
public class StatisticsEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final StatisticsServiceImpl service;

    @Autowired
    public StatisticsEndpoint(StatisticsServiceImpl service) {
        this.service = service;
    }

    @PreAuthorize("hasAuthority('STAFF_READ')")
    @PostMapping("/get")
    public StatisticsResponseDto getStatisticalData(@Valid @RequestBody StatisticsRequestDto dto) {
        LOGGER.info("GET /api/v1/statistics{}", dto.getType());
        return service.getEquipmentStatistics(dto);
    }


}
