package at.ac.tuwien.sepr.groupphase.backend.service;

//TODO: Create this interface

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;

import java.util.List;

/**
 * Service interface for managing scanning-related operations.
 */
public interface BarcodeScannerService {

    /**
     * Handles a check-out or check-in of equipment with a reservation for that equipment already existing.
     * To achieve this, the Reservation's ReservationStatus is first updated, and then each piece of Equipment's
     * RentalStatus is updated (depending on what ReservationStatus the Reservation is updated to;
     * if the new ReservationStatus is PICKED_UP, the new RentalStatus should be RENTED,
     * if the new ReservationStatus is RETURNED, the new RentalStatus should be FREE).
     *
     * @param reservationUpdateDto A DTO for the Reservation.
     * @return The updated details of the Reservation we checked in or out for.
     */
    public ReservationDetailDto checkOutOrInWithExistingReservation(ReservationUpdateDto reservationUpdateDto);

}
