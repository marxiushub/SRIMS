package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.statistics.StatisticsRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.statistics.StatisticsResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.ReservationRelation;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;

import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Pole;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Ski;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SkiBoot;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Snowboard;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.SnowboardBoot;
import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.StatisicsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Implementation of {@link StatisicsService}.
 * */
@Service
public class StatisticsServiceImpl implements StatisicsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private ReservationRepository repository;
    private final Map<EquipmentType, Class<? extends Equipment>> equipmentTypeMap;

    @Autowired
    public StatisticsServiceImpl(ReservationRepository repository) {
        this.repository = repository;
        this.equipmentTypeMap =
            Map.of(
                EquipmentType.HELMET, Helmet.class,
                EquipmentType.POLE, Pole.class,
                EquipmentType.SKI, Ski.class,
                EquipmentType.SKIBOOT, SkiBoot.class,
                EquipmentType.SNOWBOARD, Snowboard.class,
                EquipmentType.SNOWBOARDBOOT, SnowboardBoot.class
            );
    }

    /**
     * this method implements the evaluation of the number of rented days per item in the given time period.
     *
     * @param request the request DTO containing the date range and optional equipment type filter
     * @return {@link StatisticsResponseDto} with the information used for statistics in the frontend
     */
    @Override
    public StatisticsResponseDto getEquipmentStatistics(StatisticsRequestDto request) {
        LOGGER.trace("create StatisticsResponseDto for: {}", request.getType());

        LocalDate searchStart = request.getSearchStart();
        LocalDate searchEnd = request.getSearchEnd();

        List<ReservationStatus> targetStatus = List.of(
            ReservationStatus.RETURNED,
            ReservationStatus.PICKED_UP
        );

        List<Reservation> validReservations = repository.findByReservationStatusIn(targetStatus);

        List<Reservation> requiredReservations = validReservations.stream()
            .filter(reservation ->
                !reservation.getStartDate().isAfter(searchEnd)
                    && !reservation.getEndDate().isBefore(searchStart)
                    && !reservation.getCustomerProfile().getProfileName().equals("Maintenance")
            ).collect(Collectors.toList());


        Map<Long, Integer> itemCounts = new HashMap<>();
        Map<String, Integer> modelCounts = new HashMap<>();

        for (Reservation reservation : requiredReservations) {
            LocalDate actualStart = reservation.getStartDate().isAfter(searchStart)
                ? reservation.getStartDate() : searchStart;
            LocalDate actualEnd = reservation.getEndDate().isBefore(searchEnd)
                ? reservation.getEndDate() : searchEnd;

            int days = (int) ChronoUnit.DAYS.between(actualStart, actualEnd) + 1;

            List<ReservationRelation> relations = reservation.getItems();

            for (ReservationRelation item : relations) {
                Equipment equipment = item.getEquipment();

                if (request.getType() != null) {
                    Class<? extends Equipment> targetClass = equipmentTypeMap.get(request.getType());
                    if (!targetClass.isInstance(equipment)) {
                        continue;
                    }
                }

                if (request.getDetailDegree()) {
                    Long id = equipment.getId();
                    itemCounts.put(id, itemCounts.getOrDefault(id, 0) + days);
                } else {
                    String model = equipment.getModel();
                    modelCounts.put(model, modelCounts.getOrDefault(model, 0) + days);
                }
            }
        }

        StatisticsResponseDto responseDto = new StatisticsResponseDto();
        responseDto.setDetailDegree(request.getDetailDegree());

        if (request.getDetailDegree()) {
            responseDto.setItemCounts(itemCounts);
        } else {
            responseDto.setModelCounts(modelCounts);
        }

        return responseDto;
    }
}
