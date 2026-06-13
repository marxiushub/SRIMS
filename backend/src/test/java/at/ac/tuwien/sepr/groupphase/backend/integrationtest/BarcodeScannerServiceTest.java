package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import at.ac.tuwien.sepr.groupphase.backend.service.BarcodeScannerService;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.ReservationService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.BarcodeScannerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BarcodeScannerServiceTest {

    @Mock
    private EquipmentService equipmentService;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private BarcodeScannerServiceImpl barcodeScannerService;



    @Test
    void checkOutOrInWithExistingReservation_statusPickedUp_updatesReservationAndSetsEquipmentRented() {
        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setId(1L);
        updateDto.setEquipmentIds(List.of(10L, 20L));
        updateDto.setReservationStatus(ReservationStatus.PICKED_UP);

        ReservationDetailDto mockReturnDto = new ReservationDetailDto();
        mockReturnDto.setId(1L);
        mockReturnDto.setReservationStatus(ReservationStatus.PICKED_UP);

        when(reservationService.updateReservation(updateDto)).thenReturn(mockReturnDto);

        ReservationDetailDto result = barcodeScannerService.checkOutOrInWithExistingReservation(updateDto);

        assertAll(
            () -> assertThat(result).isEqualTo(mockReturnDto),
            () -> verify(reservationService, times(1)).updateReservation(updateDto),
            () -> verify(equipmentService, times(1)).updateEquipmentStatuses(List.of(10L, 20L), RentalStatus.RENTED)
        );
    }

    @Test
    void checkOutOrInWithExistingReservation_statusReturned_updatesReservationAndSetsEquipmentFree() {
        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setId(2L);
        updateDto.setEquipmentIds(List.of(30L));
        updateDto.setReservationStatus(ReservationStatus.RETURNED);

        ReservationDetailDto mockReturnDto = new ReservationDetailDto();
        mockReturnDto.setId(2L);

        when(reservationService.updateReservation(updateDto)).thenReturn(mockReturnDto);

        ReservationDetailDto result = barcodeScannerService.checkOutOrInWithExistingReservation(updateDto);

        assertAll(
            () -> assertThat(result).isEqualTo(mockReturnDto),
            () -> verify(reservationService, times(1)).updateReservation(updateDto),
            () -> verify(equipmentService, times(1)).updateEquipmentStatuses(List.of(30L), RentalStatus.FREE)
        );
    }

    @Test
    void checkOutOrInWithExistingReservation_invalidStatus_throwsIllegalArgumentException() {
        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setId(3L);
        updateDto.setEquipmentIds(List.of(40L));
        updateDto.setReservationStatus(ReservationStatus.CREATED); // CREATED is invalid for check-in/out


        when(reservationService.updateReservation(updateDto)).thenReturn(new ReservationDetailDto());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            barcodeScannerService.checkOutOrInWithExistingReservation(updateDto)
        );

        assertThat(exception.getMessage()).contains("Invalid reservation status", "CREATED");
    }

    @Test
    void checkOutWithoutExistingReservation_statusPickedUp_createsReservationAndSetsEquipmentRented() {
        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setEquipmentIds(List.of(50L, 60L));
        createDto.setReservationStatus(ReservationStatus.PICKED_UP);

        ReservationDetailDto mockReturnDto = new ReservationDetailDto();
        mockReturnDto.setId(5L);

        when(reservationService.createReservation(createDto)).thenReturn(mockReturnDto);

        ReservationDetailDto result = barcodeScannerService.checkOutWithoutExistingReservation(createDto);

        assertAll(
            () -> assertThat(result).isEqualTo(mockReturnDto),
            () -> verify(reservationService, times(1)).createReservation(createDto),
            () -> verify(equipmentService, times(1)).updateEquipmentStatuses(List.of(50L, 60L), RentalStatus.RENTED)
        );
    }

    @Test
    void checkOutWithoutExistingReservation_statusReturned_createsReservationAndSetsEquipmentFree() {
        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setEquipmentIds(List.of(70L));
        createDto.setReservationStatus(ReservationStatus.RETURNED);

        ReservationDetailDto mockReturnDto = new ReservationDetailDto();

        when(reservationService.createReservation(createDto)).thenReturn(mockReturnDto);

        ReservationDetailDto result = barcodeScannerService.checkOutWithoutExistingReservation(createDto);

        assertAll(
            () -> assertThat(result).isEqualTo(mockReturnDto),
            () -> verify(reservationService, times(1)).createReservation(createDto),
            () -> verify(equipmentService, times(1)).updateEquipmentStatuses(List.of(70L), RentalStatus.FREE)
        );
    }

    @Test
    void checkOutWithoutExistingReservation_invalidStatus_throwsIllegalArgumentException() {
        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setEquipmentIds(List.of(80L));
        createDto.setReservationStatus(ReservationStatus.CANCELLED);

        when(reservationService.createReservation(createDto)).thenReturn(new ReservationDetailDto());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            barcodeScannerService.checkOutWithoutExistingReservation(createDto)
        );

        assertThat(exception.getMessage()).contains("Invalid reservation status", "CANCELLED");
    }
}