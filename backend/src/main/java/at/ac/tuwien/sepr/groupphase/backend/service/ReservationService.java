package at.ac.tuwien.sepr.groupphase.backend.service;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    void deleteReservation(Long id, boolean isStaff);

    /**
     * Partially updates an existing reservation.
     * Only the non-null fields provided in the {@code updateDto} will be applied to the existing entity.
     *
     * @param updateDto the data transfer object containing the new values
     * @return an {@link ReservationDetailDto} representing the updated equipment
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException   if no reservation with the given ID exists in the database
     * @throws IllegalArgumentException                                           if any provided field value is invalid
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException if the provided data fails validation checks
     */
    ReservationDetailDto updateReservation(ReservationUpdateDto updateDto);

    /**
     * Performs a reservation update with the privileges of staff members.
     *
     * @param dto the data transfer object containing values to update on the reservation
     * @param isScan boolean that shows whether this update is done as part of a scan or not
     * @return an updated {@link ReservationDetailDto} reflecting the persisted changes
     * @throws NotFoundException                                                  if no reservation with the given identifier exists
     * @throws IllegalArgumentException                                           if any provided field value is invalid
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException if the provided data fails validation checks
     */
    @Transactional
    ReservationDetailDto updateReservationStaff(ReservationUpdateDto dto, boolean isScan);

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

    /**
     * Adds equipment items to an existing reservation.
     *
     * @param dto the dto used for adding equipment
     * @return a {@link ReservationDetailDto} representing the updated reservation with the added equipment
     * @throws NotFoundException if no reservation with the given ID exists in the database
     */
    ReservationDetailDto addEquipmentToReservation(ReservationAddDeleteEquipmentDto dto);

    /**
     * Removes equipment items from an existing reservation.
     *
     * @param dto the dto used for removing equipment
     * @return a {@link ReservationDetailDto} representing the updated reservation with the removed equipment
     * @throws NotFoundException if no reservation with the given ID exists in the database
     */
    ReservationDetailDto removeEquipmentFromReservation(ReservationAddDeleteEquipmentDto dto);

    /**
     * Processes reservations that are considered overdue relative to the provided boundary date.
     *
     * @param boundaryDate the date used as the cutoff for determining which reservations
     *                     are overdue; reservations with an end date on or before this
     *                     date should be processed by the implementation
     */
    void processOverdueReservations(LocalDate boundaryDate);

    /**
     * Processes and dispatches pickup reminder notifications to customers. This is a
     * mutating operation (hence {@code readOnly = false}) and may update reminder
     * state on reservations (for example marking reminders as sent). Implementations
     * are expected to run this method periodically (e.g. via a scheduled job) and
     * to ensure notifications are delivered for reservations approaching their pickup
     * date.
     */
    @Transactional(readOnly = false)
    void processPickUpReminders();
}
