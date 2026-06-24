package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationWithModeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.BarcodeScannerService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.invoke.MethodHandles;

/**
 * Represents the REST API endpoint for scanning ski-equipment via their barcodes.
 * Provides endpoint for checking out or checking in equipment via scans
 */
@RestController
@RequestMapping("/api/v1/scanner")
public class BarcodeScannerEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final BarcodeScannerService barcodeScannerService;

    @Autowired
    public BarcodeScannerEndpoint(BarcodeScannerService barcodeScannerService) {
        this.barcodeScannerService = barcodeScannerService;
    }

    /**
     * Endpoint to check out or check in equipment that's already part of a Reservation.
     *
     * @param reservationUpdateDto A DTO of the reservation that the check-in or check-out should happen for.
     * @return The updated details of the reservation we checked in or out for.
     */
    @PreAuthorize("hasAnyAuthority('CHECK_OUT_OR_IN_SCAN') and hasAuthority('STAFF')")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{id}")
    public ReservationDetailDto checkOutOrInScanWithExistingReservation(
        @PathVariable("id") Long id, @Valid @RequestBody ReservationUpdateDto reservationUpdateDto
    ) {
        LOGGER.info("PATCH /api/v1/scanner/{} - Body: {}", reservationUpdateDto.getId(), reservationUpdateDto);
        return barcodeScannerService.checkOutOrInWithExistingReservation(reservationUpdateDto);
    }

    /**
     * Endpoint to check out equipment that's not already part of a Reservation by creating a new Reservation for it.
     *
     * @param reservationCreationWithModeDto A DTO of the reservation that should be created for the check-out, including the mode.
     * @return The details of the created Reservation we checked out with.
     */
    @PreAuthorize("hasAnyAuthority('CHECK_OUT_OR_IN_SCAN') and hasAuthority('STAFF')")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public ReservationDetailDto checkOutScanWithoutExistingReservation(
        @Valid @RequestBody ReservationCreationWithModeDto reservationCreationWithModeDto
    ) {
        LOGGER.info("POST /api/v1/scanner - Body: {}", reservationCreationWithModeDto);
        return barcodeScannerService.checkOutWithoutExistingReservation(reservationCreationWithModeDto);
    }
}
