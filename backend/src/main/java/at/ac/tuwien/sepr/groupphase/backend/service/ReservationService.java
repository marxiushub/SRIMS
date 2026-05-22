package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;

import java.util.List;

/**
 * Service interface for managing reservation-related operations.
 */
public interface ReservationService {

    /**
     * Retrieves detailed information about a specific reservation based on its unique identifier.
     *
     * @param id the unique identifier of the reservation to retrieve
     * @return an {@link ReservationDetailDto} containing the detailed information of the specified reservation
     */
    ReservationDetailDto reservationById(Long id);

    /**
     * Creates a new reservation in the system based on the provided reservation data.
     *
     * @param dto the data transfer object containing the information needed to create the reservation
     * @return a {@link ReservationDetailDto} representing the created reservation entry
     */
    ReservationDetailDto createReservation(ReservationCreationDto dto);

    /**
     * Deletes a reservation from the system based on the specified ID.
     *
     * @param id the unique identifier of the reservation to delete
     */
    void deleteReservation(Long id);

    /**
     * Partially updates an existing reservation.
     * Only the non-null fields provided in the {@code updateDto} will be applied to the existing entity.
     *
     * @param id        the unique identifier of the reservation to update
     * @param updateDto the data transfer object containing the new values
     * @return an {@link ReservationDetailDto} representing the updated equipment
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException if no reservation with the given ID exists in the database
     * @throws IllegalArgumentException                                         if any provided field value is invalid
     *
     */
    ReservationDetailDto updateReservation(Long id, ReservationUpdateDto updateDto);

    /**
     * Searches for reservations based on dynamic criteria
     * All properties within the {@code searchDto} are optional. If a property is {@code null},
     * it will be ignored during the search process. If the entire DTO is {@code null} or empty,
     * all available reservations will be returned.
     *
     * @param searchDto the data transfer object containing the optional filter parameters
     * @return a list of {@link ReservationDetailDto} matching the given criteria; an empty list if no matches are found
     */
    List<ReservationDetailDto> searchReservations(ReservationSearchDto searchDto);
}
