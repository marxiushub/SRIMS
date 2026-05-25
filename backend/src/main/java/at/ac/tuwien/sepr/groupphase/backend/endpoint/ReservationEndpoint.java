package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation.EquipmentCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update.EquipmentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ReservationServiceImpl;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    @PermitAll
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
    @PermitAll
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
    @PermitAll
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping()
    public ReservationDetailDto updateReservation(
        @Valid @RequestBody ReservationUpdateDto updateDto
    ) {
        LOGGER.info("PATCH /api/v1/reservation/{} - Body: {}", updateDto.getId(), updateDto);
        return service.updateReservation(updateDto);
    }

    /**
     * Removes specific equipment from an existing reservation.
     *
     * @param dto the DTO containing the reservation ID and the equipment IDs to remove
     * @return the updated reservation as a detail DTO
     */
    @PermitAll
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
    @PermitAll
    @PostMapping("/equipment")
    @ResponseStatus(HttpStatus.OK)
    public ReservationDetailDto addEquipmentToReservation(@Valid @RequestBody ReservationAddDeleteEquipmentDto dto) {
        LOGGER.info("POST /api/v1/reservation/equipment - {}", dto);
        return service.addEquipmentToReservation(dto);
    }

}
