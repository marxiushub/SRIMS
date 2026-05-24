package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.TimePeriods;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TimePeriodsRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ReservationValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final TimePeriodsRepository timeRepo;
    private final EquipmentRepository equipmentRepository;
    private final ReservationRepository reservationRepository;
    private final CustomerProfileRepository customerProfileRepository;

    @Autowired
    public ReservationValidator(TimePeriodsRepository timeRepo, EquipmentRepository equipmentRepository, ReservationRepository reservationRepository, CustomerProfileRepository customerProfileRepository) {
        this.equipmentRepository = equipmentRepository;
        this.timeRepo = timeRepo;
        this.reservationRepository = reservationRepository;
        this.customerProfileRepository = customerProfileRepository;
    }

    public void validateUpdateDto(ReservationUpdateDto dto) {
        List<String> validationErrors = new ArrayList<>();
        List<String> notFoundErrors = new ArrayList<>();

        LocalDate pickUpDate;
        LocalDate returnDate;

        if (!reservationRepository.existsById(dto.getId())) {
            validationErrors.add("No such reservation");
        } else {
            Reservation reservation = reservationRepository.getReferenceById(dto.getId());
            pickUpDate = reservation.getPickUpDate();
            returnDate = reservation.getReturnDate();


            if (dto.getPickUpDate() != null) {
                pickUpDate = dto.getPickUpDate();
            }
            if (dto.getRentDurationDays() != null) {
                returnDate = pickUpDate.plusDays(dto.getRentDurationDays());
            }

            if (!customerProfileRepository.existsById(dto.getCustomerProfileId())) {
                notFoundErrors.add("No such CustomerProfile with id: " + dto.getCustomerProfileId());
            }

            if (!dto.getEquipmentIds().isEmpty()) {
                List<Long> doesNotExistEquip = validateEquipmentList(dto.getEquipmentIds(), validationErrors, notFoundErrors);


                for (Long equipmentId : dto.getEquipmentIds()) {
                    if (!doesNotExistEquip.contains(equipmentId)) {
                        Equipment equipment = equipmentRepository.findById(equipmentId).orElse(null);
                        isEquipmentAvailable(equipment, pickUpDate, returnDate, validationErrors, notFoundErrors);
                    }
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of horse for create failed", validationErrors);
        }

    }

    private void isEquipmentAvailable(Equipment equipment, LocalDate start, LocalDate end, List<String> validationErrors, List<String> notFoundErrors) {
        if (end.isBefore(start) || end.isEqual(start)) {
            validationErrors.add("start is after end");
        }

        List<TimePeriods> timePeriodsList = timeRepo.findByEquipment(equipment);

        for (TimePeriods time : timePeriodsList) {
            if (time.getStartDate().isBefore(end) && time.getEndDate().isAfter(start)) {
                if (time.getPeriodType() == PeriodType.RENTED) {
                    validationErrors.add("Equipment already reserved in this time range");
                } else {
                    validationErrors.add("Equipment not available at this Date");
                }
            }
        }
    }

    private List<Long>  validateEquipmentList(List<Long> equipmentList, List<String> validationErrors, List<String> notFoundErrors) {
        List <Long> doesNotExist = new ArrayList<>();

            Set<Long> buffer = new HashSet<>();
            for (Long equipId : equipmentList) {
                if (!buffer.add(equipId)) {
                    validationErrors.add("equipment with id: " + equipId + "is double in list");
                }
                if (!equipmentRepository.existsById(equipId)) {
                    notFoundErrors.add("equipment from updateList does not exists");
                    doesNotExist.add(equipId);
                }
            }

        return doesNotExist;
    }
}
