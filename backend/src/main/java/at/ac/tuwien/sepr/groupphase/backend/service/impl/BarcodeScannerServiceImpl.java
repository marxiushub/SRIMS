package at.ac.tuwien.sepr.groupphase.backend.service.impl;

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

        //TODO: Validierung dass Abholtag der Reservation heute ist, sonst kein checkOut möglich;
        // checkIn aber auch später möglich weil Verspätungen passieren können sollten

        ReservationDetailDto returnDto = reservationService.updateReservation(reservationUpdateDto);

        List<Long> equipmentIds = reservationUpdateDto.getEquipmentIds();
        ReservationStatus newReservationStatus = reservationUpdateDto.getReservationStatus();
        RentalStatus newRentalStatus;
        if (newReservationStatus.equals(ReservationStatus.PICKED_UP)) {
            newRentalStatus = RentalStatus.RENTED;
        } else if (newReservationStatus.equals(ReservationStatus.RETURNED)) {
            newRentalStatus = RentalStatus.FREE;
        } else {
            throw new IllegalArgumentException("Invalid reservation status: " + newReservationStatus
                + " should be either PICKED_UP or RETURNED when checking in/out equipment.");
        }
        equipmentService.updateEquipmentStatuses(equipmentIds, newRentalStatus);
        return returnDto;
    }

}
