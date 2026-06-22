package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ReservationServiceImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;


import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservation")
public class ReservationEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ReservationServiceImpl service;

    @Autowired
    public  ReservationEndpoint(ReservationServiceImpl service) {
        this.service = service;
    }

    /**
     * Endpoint to rent Equipment .
     *
     * @param dto an {@link at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto}
     * @return an {@link at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto}
     *
     */
    @PreAuthorize("hasAuthority('RESERVATION_CREATE')")
    @PostMapping()
    public ReservationDetailDto createReservation(@Valid @RequestBody ReservationCreationDto dto) {
        LOGGER.info("POST /api/v1/reservation - {}", dto);
        return service.createReservation(dto);
    }


    /**
     * Delete reservation.
     *
     * @param deleteDto the DTO containing the fields to update
     */
    @PreAuthorize("hasAuthority('RESERVATION_DELETE')")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping
    public void deleteReservation(
        @Valid @RequestBody ReservationAddDeleteEquipmentDto deleteDto
    ) {
        LOGGER.info("DELETE /api/v1/reservation - Body: {}", deleteDto);

        service.deleteReservation(deleteDto.getId());
    }

    /**
     * Partially updates an existing reservation.
     *
     * @param updateDto the DTO containing the fields to update
     * @return the updated equipment as a detail DTO
     */
    @PreAuthorize("hasAuthority('RESERVATION_UPDATE')")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{id}")
    public ReservationDetailDto updateReservation(
        @Valid @RequestBody ReservationUpdateDto updateDto
    ) {
        LOGGER.info("PATCH /api/v1/reservation/{} - Body: {}", updateDto.getId(), updateDto);
        return service.updateReservation(updateDto);
    }

    /**
     * Partially updates an existing reservation with the privileges of a Staff member.
     *
     * @param updateDto the DTO containing the fields to update
     * @return the updated equipment as a detail DTO
     */
    @PreAuthorize("hasAuthority('RESERVATION_UPDATE') and hasAuthority('STAFF')")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/staff/{id}")
    public ReservationDetailDto updateReservationStaff(
        @Valid @RequestBody ReservationUpdateDto updateDto
    ) {
        LOGGER.info("PATCH /api/v1/reservation/staff/{} - Body: {}", updateDto.getId(), updateDto);
        return service.updateReservationStaff(updateDto);
    }

    /**
     * Removes specific equipment from an existing reservation.
     *
     * @param dto the DTO containing the reservation ID and the equipment IDs to remove
     * @return the updated reservation as a detail DTO
     */
    @PreAuthorize("hasAuthority('RESERVATION_UPDATE')")
    @DeleteMapping("/equipment")
    @ResponseStatus(HttpStatus.OK)
    public ReservationDetailDto removeEquipmentFromReservation(@Valid @RequestBody ReservationAddDeleteEquipmentDto dto) {
        LOGGER.info("DELETE /api/v1/reservation/equipment - {}", dto);
        return service.removeEquipmentFromReservation(dto);
    }

    /**
     * Adds specific equipment to an existing reservation.
     *
     * @param dto the DTO containing the reservation ID and the equipment IDs to add
     * @return the updated reservation as a detail DTO
     */
    @PreAuthorize("hasAuthority('RESERVATION_UPDATE')")
    @PostMapping("/equipment")
    @ResponseStatus(HttpStatus.OK)
    public ReservationDetailDto addEquipmentToReservation(@Valid @RequestBody ReservationAddDeleteEquipmentDto dto) {
        LOGGER.info("POST /api/v1/reservation/equipment - {}", dto);
        return service.addEquipmentToReservation(dto);
    }

    /**
     * Searches for reservations based on optional filter criteria.
     * If no criteria are provided, all reservations might be returned (depending on service logic).
     *
     * @param searchDto the DTO containing the search parameters (mapped from URL query parameters)
     * @return a list of reservations matching the criteria
     */
    @PreAuthorize("hasAuthority('RESERVATION_SEARCH')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationDetailDto> searchReservations(ReservationSearchDto searchDto) {
        LOGGER.info("GET /api/v1/reservation - Search parameters: {}", searchDto);
        return service.searchReservations(searchDto);
    }

    /**
     * Endpoint to retrieve a specific reservation by its unique id.
     *
     * @param id the unique id of the reservation to be retrieved
     * @return an {@link ReservationDetailDto} representing the reservation information for the specified id
     */
    @PreAuthorize("hasAuthority('RESERVATION_READ')")
    @GetMapping("/{id}")
    public ReservationDetailDto getReservationById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/reservation/{}", id);
        return service.reservationById(id);
    }

}
