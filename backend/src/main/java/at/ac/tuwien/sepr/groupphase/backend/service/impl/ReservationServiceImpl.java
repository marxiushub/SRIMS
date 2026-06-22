package at.ac.tuwien.sepr.groupphase.backend.service.impl;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ReservationMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.ReservationRelation;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.CurrentUserService;
import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationServiceImpl implements at.ac.tuwien.sepr.groupphase.backend.service.ReservationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ReservationMapper reservationMapper;
    private final ReservationValidator validator;
    private final ReservationRepository reservationRepository;
    private final EquipmentRepository equipmentRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final EmailService emailService;
    private final CurrentUserService currentUserService;

    @Autowired
    public ReservationServiceImpl(ReservationMapper reservationMapper,
                                  ReservationRepository reservationRepository,
                                  EquipmentRepository equipmentRepository,
                                  CustomerProfileRepository customerProfileRepository,
                                  ReservationValidator validator,
                                  EmailService emailService,
                                  CurrentUserService currentUserService) {
        this.reservationMapper = reservationMapper;
        this.reservationRepository = reservationRepository;
        this.equipmentRepository = equipmentRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.validator = validator;
        this.emailService = emailService;
        this.currentUserService = currentUserService;
    }


    @Override
    @Transactional
    public ReservationDetailDto reservationById(Long id) {
        LOGGER.trace("Get reservation by id: {}", id);
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Reservation with ID " + id + " was not found."));
        return reservationMapper.entityToDetailDto(reservation);
    }

    @Override
    @Transactional
    public ReservationDetailDto createReservation(ReservationCreationDto dto) {
        LOGGER.trace("create reservation");

        validator.validateCreateDto(dto);

        CustomerProfile profile = customerProfileRepository.getReferenceById(dto.getCustomerProfileId());

        Reservation reservation = new Reservation(
            profile,
            dto.getPickUpTime(),
            dto.getStartDate(),
            dto.getEndDate(),
            dto.getReservationStatus()
        );
        List<Equipment> equipmentList = equipmentRepository.findAllById(dto.getEquipmentIds());
        for (Equipment equipment : equipmentList) {
            reservation.addItem(equipment);

            equipment.addTimePeriod(dto.getStartDate(), dto.getEndDate(), PeriodType.RENTED, reservation);
        }
        calculateAndSetTotalPrice(reservation);
        Reservation savedReservation = reservationRepository.save(reservation);
        emailService.sendReservationConfirmation(equipmentList, savedReservation);
        reservation.setConfirmationEmailSent();
        return reservationMapper.entityToDetailDto(savedReservation);
    }

    @Override
    @Transactional
    public void deleteReservation(Long id, boolean isStaff) {
        LOGGER.trace("Deleting reservation with id {}", id);

        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Reservation with ID " + id + " was not found."));

        if (!isStaff) {
            currentUserService.getUserId();
            if (!reservation.getCustomerProfile().getCustomer().getId().equals(currentUserService.getUserId())) {
                throw new AccessDeniedException("You are not allowed to perform this action.");
            }
            if (reservation.getStartDate().isBefore(LocalDate.now().plusDays(2))) {
                throw new ValidationException("Reservations can only be deleted at least two days before the start date.");
            }
        }

        deleteTimePeriodsForEquipment(reservation.getItems().stream().map(ReservationRelation::getEquipment).toList(), reservation);

        reservationRepository.delete(reservation);

    }

    @Override
    @Transactional
    public ReservationDetailDto updateReservation(ReservationUpdateDto dto) {
        LOGGER.trace("update reservation {} with customer permissions", dto.getId());

        Long userId = currentUserService.getUserId();

        validator.validateUpdateDto(dto, userId);

        Reservation reservation = reservationRepository.getReferenceById(dto.getId());

        applyUpdateCommon(reservation, dto, false);

        calculateAndSetTotalPrice(reservation);
        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.entityToDetailDto(saved);
    }

    @Transactional
    @Override
    public ReservationDetailDto updateReservationStaff(ReservationUpdateDto dto) {
        LOGGER.trace("update reservation {} with staff permissions", dto.getId());


        validator.validateUpdateDto(dto, null);

        Reservation reservation = reservationRepository.getReferenceById(dto.getId());

        applyUpdateCommon(reservation, dto, true);

        calculateAndSetTotalPrice(reservation);
        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.entityToDetailDto(saved);
    }

    /**
     * Shared update logic between customer and staff update methods.
     * If {@code isStaff} is true, staff-specific behavior (setting reservationStatus and
     * removing time periods when changed to RETURNED/CANCELLED) is applied.
     */
    private void applyUpdateCommon(Reservation reservation, ReservationUpdateDto dto, boolean isStaff) {
        boolean datesChanged = (dto.getStartDate() != null && !dto.getStartDate().equals(reservation.getStartDate()))
            || (dto.getEndDate() != null && !dto.getEndDate().equals(reservation.getEndDate()));
        boolean equipmentChanged = dto.getEquipmentIds() != null;

        // If the startDate and/or endDate of the Reservation changes, or if the included Equipments is changed,
        // delete corresponding old timePeriods of the Equipments included in the Reservation
        if (datesChanged || equipmentChanged) {
            List<Equipment> currentEquipments = reservation.getItems().stream()
                .map(ReservationRelation::getEquipment).toList();
            deleteTimePeriodsForEquipment(currentEquipments, reservation);
        }

        if (dto.getPickUpTime() != null) {
            reservation.setPickUpTime(dto.getPickUpTime());
        }
        if (dto.getStartDate() != null) {
            reservation.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            reservation.setEndDate(dto.getEndDate());
        }

        if (isStaff && dto.getReservationStatus() != null) {
            reservation.setReservationStatus(dto.getReservationStatus());
        }

        if (dto.getCustomerProfileId() != null) {
            reservation.setCustomerProfile(customerProfileRepository.getReferenceById(dto.getCustomerProfileId()));
        }

        List<Equipment> finalEquipmentsToReserve;
        if (equipmentChanged) {
            reservation.getItems().clear();
            finalEquipmentsToReserve = equipmentRepository.findAllById(dto.getEquipmentIds());
            for (Equipment eq : finalEquipmentsToReserve) {
                reservation.addItem(eq);
            }
        } else {
            finalEquipmentsToReserve = reservation.getItems().stream()
                .map(ReservationRelation::getEquipment).toList();
        }

        // If the startDate or endDate of the Reservation were changed, or if the included Equipments were changed,
        // add new timePeriods for the Equipments included in the Reservation (as the old ones were deleted above)
        if (datesChanged || equipmentChanged) {
            LocalDate newStart = reservation.getStartDate();
            LocalDate newEnd = reservation.getEndDate();

            for (Equipment equipment : finalEquipmentsToReserve) {
                equipment.addTimePeriod(newStart, newEnd, PeriodType.RENTED, reservation);
            }
        }

        // Staff-only: If the ReservationStatus is changed to RETURNED or CANCELLED, delete all corresponding timePeriods
        // of the included Equipment to free the Equipment up again and reduce the size of the Equipment-table in the database
        if (isStaff && dto.getReservationStatus() != null) {
            if (dto.getReservationStatus() == ReservationStatus.RETURNED
                || dto.getReservationStatus() == ReservationStatus.CANCELLED) {

                List<Equipment> currentEquipments = reservation.getItems().stream()
                    .map(ReservationRelation::getEquipment).toList();
                deleteTimePeriodsForEquipment(currentEquipments, reservation);
            }
        }

    }

    @Transactional
    @Override
    public List<ReservationDetailDto> searchReservations(ReservationSearchDto searchDto) {
        LOGGER.trace("Search reservations with filter {}", searchDto);

        if (searchDto == null) {
            searchDto = new ReservationSearchDto();
        }

        final ReservationSearchDto finalSearchDto = searchDto;

        boolean isStaff = currentUserService.hasAuthority("STAFF");

        Long effectiveAccountId = searchDto.getAccountId();

        if (!isStaff) {
            Long currentUserId = currentUserService.getUserId();

            if (effectiveAccountId != null && !effectiveAccountId.equals(currentUserId)) {
                throw new AccessDeniedException("Cannot search reservations of another customer.");
            }

            // Customer darf nur sich selbst sehen
            effectiveAccountId = currentUserId;
        }

        Specification<Reservation> spec = (root, query, cb) -> cb.conjunction();

        final Long accountId = effectiveAccountId;

        if (accountId != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("customerProfile").get("customer").get("id"), accountId)
            );
        }

        if (finalSearchDto.getStartDate() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("startDate"), finalSearchDto.getStartDate())
            );
        }

        if (finalSearchDto.getEndDate() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("endDate"), finalSearchDto.getEndDate())
            );
        }

        if (finalSearchDto.getPickUpTime() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("pickUpTime"), finalSearchDto.getPickUpTime())
            );
        }

        if (finalSearchDto.getSearchRangeStart() != null) {
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("endDate"), finalSearchDto.getSearchRangeStart())
            );
        }

        if (finalSearchDto.getSearchRangeEnd() != null) {
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("startDate"), finalSearchDto.getSearchRangeEnd())
            );
        }

        if (finalSearchDto.getEquipmentIds() != null && !finalSearchDto.getEquipmentIds().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);

                return root.join("items").join("equipment").get("id")
                    .in(finalSearchDto.getEquipmentIds());
            });
        }

        if (finalSearchDto.getReservationStatus() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("reservationStatus"), finalSearchDto.getReservationStatus())
            );
        }

        List<Reservation> foundReservations = reservationRepository.findAll(spec);

        return foundReservations.stream()
            .map(reservationMapper::entityToDetailDto)
            .toList();
    }

    @Override
    @Transactional
    public ReservationDetailDto addEquipmentToReservation(ReservationAddDeleteEquipmentDto dto) {

        validator.validateReservationAddEquip(dto);

        Reservation reservation = reservationRepository.getReferenceById(dto.getId());
        List<Equipment> equipmentList = equipmentRepository.findAllById(dto.getEquipmentIds());

        for (Equipment equipment : equipmentList) {
            reservation.addItem(equipment);
            equipment.addTimePeriod(reservation.getStartDate(), reservation.getEndDate(), PeriodType.RENTED, reservation);
        }

        calculateAndSetTotalPrice(reservation);
        return reservationMapper.entityToDetailDto(reservation);
    }

    @Override
    @Transactional
    public ReservationDetailDto removeEquipmentFromReservation(ReservationAddDeleteEquipmentDto dto) {
        validator.validateReservationRemoveEquipment(dto);

        Reservation reservation = reservationRepository.getReferenceById(dto.getId());
        List<Equipment> equipmentList = equipmentRepository.findAllById(dto.getEquipmentIds());

        deleteTimePeriodsForEquipment(equipmentList, reservation);

        reservation.getItems().removeIf(relation -> dto.getEquipmentIds().contains(relation.getEquipment().getId()));

        calculateAndSetTotalPrice(reservation);
        Reservation savedReservation = reservationRepository.save(reservation);
        return reservationMapper.entityToDetailDto(savedReservation);
    }

    @Transactional(readOnly = false)
    @Override
    public void processOverdueReservations(LocalDate boundaryDate) {

        List<Reservation> overdueReservations = reservationRepository
            .findByEndDateBeforeAndReservationStatusAndOverdueReminderSentFalse(
                boundaryDate, ReservationStatus.PICKED_UP);

        if (overdueReservations.isEmpty()) {
            LOGGER.info("No overdue reservations found for boundary date: {}", boundaryDate);
            return;
        }

        LOGGER.info("Found {} overdue reservations! Starting reminder process...", overdueReservations.size());

        for (Reservation res : overdueReservations) {
            try {

                List<Equipment> currentEquipment = res.getItems().stream()
                    .map(ReservationRelation::getEquipment).toList();

                emailService.sendOverdueReminder(currentEquipment, res);

                res.setOverdueReminderSent(true);
                reservationRepository.save(res);

                LOGGER.info("Overdue reminder successfully sent to {} (Reservation ID: {}).",
                    res.getCustomerProfile().getCustomer().getEmail(), res.getId());

            } catch (Exception e) {
                LOGGER.error("Failed to process overdue reminder for Reservation ID {}: {}", res.getId(), e.getMessage());
            }
        }
    }

    @Transactional(readOnly = false)
    @Override
    public void processPickUpReminders() {
        LocalDate today = LocalDate.now();
        LocalDate boundaryDate = today.plusDays(2);

        List<Reservation> upcomingReservations = reservationRepository
            .findByStartDateBetweenAndReservationStatusAndPickUpReminderSentFalse(
                today, boundaryDate, ReservationStatus.CREATED);

        if (upcomingReservations.isEmpty()) {
            LOGGER.info("No upcoming reservations found needing a pick-up reminder.");
            return;
        }

        LOGGER.info("Found {} upcoming reservations. Starting pick-up reminder process...", upcomingReservations.size());

        for (Reservation res : upcomingReservations) {
            try {

                List<Equipment> currentEquipment = res.getItems().stream()
                    .map(ReservationRelation::getEquipment).toList();

                emailService.sendPickUpReminderEmail(currentEquipment, res);

                res.setPickUpReminderSent(true);
                reservationRepository.save(res);

                LOGGER.info("Pick-up reminder successfully sent to {} (Reservation ID: {}).",
                    res.getCustomerProfile().getCustomer().getEmail(), res.getId());

            } catch (Exception e) {
                LOGGER.error("Failed to send pick-up reminder for Reservation ID {}: {}", res.getId(), e.getMessage());
            }
        }
    }

    private void deleteTimePeriodsForEquipment(List<Equipment> equipmentList, Reservation reservation) {
        for (Equipment equipment : equipmentList) {
            equipment.getTimePeriodsList().removeIf(tp ->
                tp.getPeriodType() == PeriodType.RENTED
                    && tp.getReservation() != null
                    && tp.getReservation().getId().equals(reservation.getId())
            );
        }
    }

    private void calculateAndSetTotalPrice(Reservation reservation) {
        if (reservation.getStartDate() == null || reservation.getEndDate() == null || reservation.getItems().isEmpty()) {
            reservation.setTotalPrice(0.0);
            return;
        }

        long days = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate()) + 1;

        double equipmentSum = reservation.getItems().stream()
            .filter(item -> item.getEquipment() != null)
            .mapToDouble(item -> item.getEquipment().getPrice())
            .sum();

        reservation.setTotalPrice(equipmentSum * days);
    }


}
