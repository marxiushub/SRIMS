package at.ac.tuwien.sepr.groupphase.backend.service.impl;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.ReservationRelation;
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


        LocalDate pickUpDate;
        LocalDate returnDate;

        if (dto == null) {
            throw new ValidationException(
                "Reservation update dto must not be null",
                List.of("dto is null")
            );
        }

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

            if (dto.getCustomerProfileId() == null) {
                validationErrors.add("CustomerProfileId must not be null");
            } else if (!customerProfileRepository.existsById(dto.getCustomerProfileId())) {
                validationErrors.add(
                    "No such CustomerProfile with id: "
                        + dto.getCustomerProfileId()
                );
            }

            if (dto.getEquipmentIds() != null) {
                List<Long> doesNotExistEquip = validateEquipmentList(dto.getEquipmentIds(), validationErrors);

                if (returnDate.isBefore(pickUpDate)) {
                    validationErrors.add("start is after end");
                } else {
                    for (Long equipmentId : dto.getEquipmentIds()) {
                        if (!doesNotExistEquip.contains(equipmentId)) {
                            Equipment equipment = equipmentRepository.findById(equipmentId).orElse(null);
                            isEquipmentAvailable(equipment, pickUpDate, returnDate, validationErrors);
                        }
                    }

                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of the dto for update failed", validationErrors);
        }


    }

    public void validateReservationAddEquip(ReservationAddDeleteEquipmentDto dto) {

        List<String> validationErrors = new ArrayList<>();

        if (dto == null) {
            throw new IllegalArgumentException("dto must not be null");
        }

        Reservation reservation = reservationRepository.findById(dto.getId()).orElseThrow(() ->
            new NotFoundException("Reservation with ID " + dto.getId() + " not found.")
        );
        LocalDate start = reservation.getPickUpDate();
        LocalDate end = reservation.getReturnDate();


        if (dto.getEquipmentIds() != null) {
            for (Long id : dto.getEquipmentIds()) {
                Equipment equipment = equipmentRepository.findById(id)
                    .orElseThrow(() ->
                        new NotFoundException("Equipment with ID " + id + " not found.")
                   );
                isEquipmentAvailable(equipment, start, end, validationErrors);
            }
        }
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(
                "Validation failed for adding equipment",
                validationErrors
            );
        }
    }

    public void validateReservationRemoveEquipment(ReservationAddDeleteEquipmentDto dto) {

        List<String> validationErrors = new ArrayList<>();

        if (dto == null) {
            throw new IllegalArgumentException("dto must not be null");
        }

        Reservation reservation = reservationRepository.findById(dto.getId()).orElseThrow(() ->
            new NotFoundException("Reservation with ID " + dto.getId() + " not found.")
        );

        if (dto.getEquipmentIds() != null) {
            validateEquipmentList(dto.getEquipmentIds(), validationErrors);


            for (Long equipmentId : dto.getEquipmentIds()) {
                boolean equipmentInReservation = false;
                for (ReservationRelation item : reservation.getItems()) {
                    if (item.getEquipment().getId().equals(equipmentId)) {
                        equipmentInReservation = true;
                        break;
                    }
                }
                if (!equipmentInReservation) {
                    validationErrors.add("Equipment with ID " + equipmentId + " is not part of this reservation");
                }
            }
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of the dto for update failed", validationErrors);
        }

    }


    private void isEquipmentAvailable(Equipment equipment, LocalDate start, LocalDate end, List<String> validationErrors) {
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

    private List<Long>  validateEquipmentList(List<Long> equipmentList, List<String> validationErrors) {
        List<Long> doesNotExist = new ArrayList<>();

        Set<Long> buffer = new HashSet<>();
        for (Long equipId : equipmentList) {
            if (!buffer.add(equipId)) {
                validationErrors.add("equipment with id: " + equipId + "is double in list");
            }
            if (!equipmentRepository.existsById(equipId)) {
                validationErrors.add("equipment from updateList does not exists");
                doesNotExist.add(equipId);
            }
        }

        return doesNotExist;
    }

}
