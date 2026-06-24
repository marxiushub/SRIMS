package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationWithModeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import at.ac.tuwien.sepr.groupphase.backend.service.BarcodeScannerService;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Implementation of {@link BarcodeScannerService} for handling scanning-related operations.
 */
@Service
public class BarcodeScannerServiceImpl implements BarcodeScannerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final EquipmentService equipmentService;
    private final ReservationService reservationService;

    /**
     * Constructor for BarcodeScannerService. Initializes the service with the necessary services it should be able to call.
     *
     * @param equipmentService  The EquipmentService the BarcodeScannerService should call on.
     * @param reservationService The ReservationService the BarcodeScannerService should call on.
     */
    @Autowired
    public BarcodeScannerServiceImpl(EquipmentService equipmentService,
                                     ReservationService reservationService) {
        this.equipmentService = equipmentService;
        this.reservationService = reservationService;
    }

    @Override
    @Transactional
    public ReservationDetailDto checkOutOrInWithExistingReservation(ReservationUpdateDto reservationUpdateDto) {
        LOGGER.info("CheckIn or Checkout with Existing Reservation with ReservationID " + reservationUpdateDto.getId());

        ReservationDetailDto returnDto = reservationService.updateReservationStaff(reservationUpdateDto);
        List<Long> equipmentIds = reservationUpdateDto.getEquipmentIds();
        RentalStatus newRentalStatus = getNewRentalStatusForNewReservationStatus(reservationUpdateDto.getReservationStatus());
        equipmentService.updateEquipmentStatuses(equipmentIds, newRentalStatus);
        return returnDto;
    }

    @Override
    @Transactional
    public ReservationDetailDto checkOutWithoutExistingReservation(ReservationCreationWithModeDto reservationCreationWithModeDto) {
        LOGGER.info("Checkout without Existing Reservation, mode={}", reservationCreationWithModeDto.getMode());

        if ("MAINTENANCE".equalsIgnoreCase(reservationCreationWithModeDto.getMode())
            && reservationCreationWithModeDto.getEquipmentIds() != null && reservationCreationWithModeDto.getEquipmentIds().size() > 1) {
            throw new IllegalArgumentException(
                "Maintenance checkout only allows a single equipment item, but "
                    + reservationCreationWithModeDto.getEquipmentIds().size() + " were given.");
        }

        ReservationDetailDto returnDto = reservationService.createReservation(reservationCreationWithModeDto.toReservationCreationDto());
        List<Long> equipmentIds = reservationCreationWithModeDto.getEquipmentIds();

        RentalStatus newRentalStatus = "MAINTENANCE".equalsIgnoreCase(reservationCreationWithModeDto.getMode())
            ? RentalStatus.MAINTENANCE
            : getNewRentalStatusForNewReservationStatus(reservationCreationWithModeDto.getReservationStatus());

        equipmentService.updateEquipmentStatuses(equipmentIds, newRentalStatus);
        return returnDto;
    }

    //Helper-Method to determine the RentalStatus for Equipment corresponding to the ReservationStatus of the
    // Reservation
    private RentalStatus getNewRentalStatusForNewReservationStatus(ReservationStatus newReservationStatus) {
        RentalStatus newRentalStatus;
        if (newReservationStatus.equals(ReservationStatus.PICKED_UP)) {
            newRentalStatus = RentalStatus.RENTED;
        } else if (newReservationStatus.equals(ReservationStatus.RETURNED)) {
            newRentalStatus = RentalStatus.FREE;
        } else {
            throw new IllegalArgumentException("Invalid reservation status: " + newReservationStatus
                + " should be either PICKED_UP or RETURNED when checking in/out equipment.");
        }
        return newRentalStatus;
    }

}
