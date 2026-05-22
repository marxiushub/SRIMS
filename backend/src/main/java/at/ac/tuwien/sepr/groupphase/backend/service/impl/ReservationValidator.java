package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.TimePeriods;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;

@Component
public class ReservationValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public ReservationValidator() {
    }

    public void isEquipmentAvailable(Equipment equipment, LocalDate start, LocalDate end) throws IllegalArgumentException, ValidationException {
        if (equipment == null || start == null || end == null) {
            throw new IllegalArgumentException("Equipment and dates must not be null");
        }

        if (end.isBefore(start) || end.isEqual(start)) {
            throw new IllegalArgumentException("End must be after start");
        }

        List<TimePeriods> timePeriodsList = equipment.getTimePeriodsList();

        for (TimePeriods time : timePeriodsList) {
            if (time.getStartDate().isBefore(end) && time.getEndDate().isAfter(start)) {
                if (time.getPeriodType() == PeriodType.RENTED) {
                    throw new ValidationException("Equipment already reserved in this time range");
                } else {
                    throw new ValidationException("Equipment not available at this Date");
                }
            }
        }


    }


}
