package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation.EquipmentCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update.EquipmentUpdateDto;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PathVariable;

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
     * Partially updates an existing reservation.
     *
     * @param id the ID of the equipment to update
     * @param updateDto the DTO containing the fields to update
     * @return the updated equipment as a detail DTO
     */
    @PermitAll
    @ResponseStatus(HttpStatus.OK)
    public ReservationDetailDto updatereservation(
        @PathVariable("id") Long id,
        @Valid @RequestBody ReservationUpdateDto updateDto
    ) {
        LOGGER.info("PATCH /api/v1/reservation/{} - Body: {}", id, updateDto);
        return service.updateReservation(updateDto);
    }


}
