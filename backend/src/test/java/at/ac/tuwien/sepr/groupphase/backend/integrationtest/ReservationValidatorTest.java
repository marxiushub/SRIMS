package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.LocalizedError;
import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ReservationValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationValidatorTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @InjectMocks
    private ReservationValidator validator;

    private static void assertContainsErrorMessage(ValidationException ex, String expectedMessage) {
        assertThat(ex.getErrors())
            .extracting(LocalizedError::message)
            .contains(expectedMessage);
    }

    private static void assertContainsErrorMessageContaining(ValidationException ex, String expectedMessagePart) {
        assertThat(ex.getErrors())
            .extracting(LocalizedError::message)
            .anyMatch(message -> message.contains(expectedMessagePart));
    }

    @Test
    void allMethods_withNullDto_throwsException() {
        assertThrows(ValidationException.class, () -> validator.validateCreateDto(null));
        assertThrows(ValidationException.class, () -> validator.validateUpdateDto(null, null));
        assertThrows(IllegalArgumentException.class, () -> validator.validateReservationAddEquip(null));
        assertThrows(IllegalArgumentException.class, () -> validator.validateReservationRemoveEquipment(null));
    }


    @Test
    void validateCreateDto_withEndDateBeforeStartDate_throwsValidationException() {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now());
        dto.setCustomerProfileId(1L);
        dto.setEquipmentIds(List.of(10L));
        dto.setReservationStatus(ReservationStatus.CREATED);

        when(customerProfileRepository.existsById(1L)).thenReturn(true);
        when(equipmentRepository.existsById(10L)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreateDto(dto));
        assertContainsErrorMessage(ex, "End date is before start date");
    }

    @Test
    void validateCreateDto_withEmptyEquipmentList_throwsValidationException() {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(2));
        dto.setCustomerProfileId(1L);
        dto.setEquipmentIds(List.of());
        dto.setReservationStatus(ReservationStatus.CREATED);

        when(customerProfileRepository.existsById(1L)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreateDto(dto));
        assertContainsErrorMessage(ex, "A reservation must contain at least one equipment.");
    }

    @Test
    void validateCreateDto_withDuplicateEquipmentIds_throwsValidationException() {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(2));
        dto.setCustomerProfileId(1L);
        dto.setEquipmentIds(List.of(10L, 10L));
        dto.setReservationStatus(ReservationStatus.CREATED);

        when(customerProfileRepository.existsById(1L)).thenReturn(true);
        when(equipmentRepository.existsById(10L)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreateDto(dto));
        assertContainsErrorMessageContaining(ex, "is double in list");
    }

    @Test
    void validateCreateDto_withNonExistentEquipment_throwsValidationException() {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(2));
        dto.setCustomerProfileId(1L);
        dto.setEquipmentIds(List.of(99L));
        dto.setReservationStatus(ReservationStatus.CREATED);

        when(customerProfileRepository.existsById(1L)).thenReturn(true);
        when(equipmentRepository.existsById(99L)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreateDto(dto));
        assertContainsErrorMessage(ex, "equipment from updateList does not exists");
    }


    @Test
    void validateCreateDto_withOverlappingRentedPeriod_throwsValidationException() {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setStartDate(LocalDate.now().plusDays(2));
        dto.setEndDate(LocalDate.now().plusDays(5));
        dto.setCustomerProfileId(1L);
        dto.setEquipmentIds(List.of(10L));
        dto.setReservationStatus(ReservationStatus.CREATED);

        Equipment mockEquipment = new Helmet("Test Helmet", 10.0, 50.0, RentalStatus.FREE, SkillLevel.BEGINNER);
        org.springframework.test.util.ReflectionTestUtils.setField(mockEquipment, "id", 10L);

        mockEquipment.addTimePeriod(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), PeriodType.RENTED, null);

        when(customerProfileRepository.existsById(1L)).thenReturn(true);
        when(equipmentRepository.existsById(10L)).thenReturn(true);
        when(equipmentRepository.findAllByIdsLocked(List.of(10L))).thenReturn(List.of(mockEquipment));

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreateDto(dto));
        assertContainsErrorMessageContaining(ex, "is already reserved in this time range");
    }

    @Test
    void validateCreateDto_withOverlappingRepairPeriod_throwsValidationException() {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setStartDate(LocalDate.now().plusDays(2));
        dto.setEndDate(LocalDate.now().plusDays(5));
        dto.setCustomerProfileId(1L);
        dto.setEquipmentIds(List.of(10L));
        dto.setReservationStatus(ReservationStatus.CREATED);

        Equipment mockEquipment = new Helmet("Test Helmet", 10.0, 50.0, RentalStatus.FREE, SkillLevel.BEGINNER);
        org.springframework.test.util.ReflectionTestUtils.setField(mockEquipment, "id", 10L);

        mockEquipment.addTimePeriod(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), PeriodType.REPAIR, null);

        when(customerProfileRepository.existsById(1L)).thenReturn(true);
        when(equipmentRepository.existsById(10L)).thenReturn(true);
        when(equipmentRepository.findAllByIdsLocked(List.of(10L))).thenReturn(List.of(mockEquipment));

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreateDto(dto));
        assertContainsErrorMessageContaining(ex, "is not available at this date");
    }


    @Test
    void validateReservationRemoveEquipment_withEquipmentNotInReservation_throwsValidationException() {
        ReservationAddDeleteEquipmentDto dto = new ReservationAddDeleteEquipmentDto();
        dto.setId(1L);
        dto.setEquipmentIds(List.of(99L));

        Reservation reservation = new Reservation(null, null, null, null, null);
        org.springframework.test.util.ReflectionTestUtils.setField(reservation, "items", new java.util.ArrayList<>());
        org.springframework.test.util.ReflectionTestUtils.setField(reservation, "id", 1L); // Auch hier Reflection nutzen!

        Equipment insideEquip = new Helmet("Test Helmet", 10.0, 50.0, RentalStatus.FREE, SkillLevel.BEGINNER);
        org.springframework.test.util.ReflectionTestUtils.setField(insideEquip, "id", 5L);

        reservation.addItem(insideEquip);

        when(reservationRepository.findByIdLocked(1L)).thenReturn(Optional.of(reservation));
        when(equipmentRepository.existsById(99L)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateReservationRemoveEquipment(dto));
        assertContainsErrorMessage(ex, "Equipment with ID 99 is not part of this reservation");
    }

    @Test
    void validateCreateDto_withNullFields_accumulatesErrors() {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setStartDate(null);
        dto.setEndDate(null);
        dto.setCustomerProfileId(null);
        dto.setEquipmentIds(null);
        dto.setReservationStatus(null);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreateDto(dto));

        assertAll(
            () -> assertContainsErrorMessage(ex, "No such CustomerProfile with id: null"),
            () -> assertContainsErrorMessage(ex, "A reservation must contain at least one equipment."),
            () -> assertContainsErrorMessage(ex, "Reservation status must not be null")
        );
    }

    @Test
    void validateUpdateDto_withVariousErrors_throwsValidationException() {
        ReservationUpdateDto dto = new ReservationUpdateDto();
        dto.setId(1L);
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now());
        dto.setCustomerProfileId(99L);
        dto.setEquipmentIds(List.of());

        Reservation reservation = new Reservation(null, null, LocalDate.now(), LocalDate.now().plusDays(1), null);
        org.springframework.test.util.ReflectionTestUtils.setField(reservation, "id", 1L);

        when(reservationRepository.findByIdLocked(1L)).thenReturn(java.util.Optional.of(reservation));
        when(customerProfileRepository.existsById(99L)).thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateUpdateDto(dto, null));

        assertAll(
            () -> assertContainsErrorMessage(ex, "End date is before start date"),
            () -> assertContainsErrorMessage(ex, "No such CustomerProfile with id: 99"),
            () -> assertContainsErrorMessage(ex, "A reservation must contain at least one equipment.")
        );
    }

    @Test
    void validateReservationAddEquip_withUnknownReservationId_throwsNotFoundException() {
        ReservationAddDeleteEquipmentDto dto = new ReservationAddDeleteEquipmentDto();
        dto.setId(99L);
        dto.setEquipmentIds(List.of(10L));
        NotFoundException ex = assertThrows(NotFoundException.class,
            () -> validator.validateReservationAddEquip(dto));
        assertThat(ex.getMessage()).contains("Reservation with ID 99 not found.");
    }

    @Test
    void validateReservationAddEquip_withUnknownEquipmentId_throwsValidationException() {
        ReservationAddDeleteEquipmentDto dto = new ReservationAddDeleteEquipmentDto();
        dto.setId(1L);
        dto.setEquipmentIds(List.of(99L));

        Reservation reservation = new Reservation(null, null, LocalDate.now(), LocalDate.now().plusDays(2), null);
        org.springframework.test.util.ReflectionTestUtils.setField(reservation, "id", 1L);

        when(reservationRepository.findByIdLocked(anyLong())).thenReturn(Optional.of(reservation));

        when(reservationRepository.findByIdLocked(anyLong())).thenReturn(Optional.of(reservation));
        when(equipmentRepository.existsById(anyLong())).thenReturn(false);


        assertThrows(ValidationException.class,
            () -> validator.validateReservationAddEquip(dto));
    }

    @Test
    void validateReservationAddEquip_withValidData_doesNotThrow() {
        ReservationAddDeleteEquipmentDto dto = new ReservationAddDeleteEquipmentDto();
        dto.setId(1L);
        dto.setEquipmentIds(List.of(10L));

        Reservation reservation = new Reservation(null, null, LocalDate.now(), LocalDate.now().plusDays(2), null);
        org.springframework.test.util.ReflectionTestUtils.setField(reservation, "id", 1L);

        Equipment equipment = new Helmet("Test Helmet", 10.0, 50.0, RentalStatus.FREE, SkillLevel.BEGINNER);
        org.springframework.test.util.ReflectionTestUtils.setField(equipment, "id", 10L);

        when(reservationRepository.findByIdLocked(anyLong())).thenReturn(Optional.of(reservation));
        when(equipmentRepository.existsById(anyLong())).thenReturn(true);
        when(equipmentRepository.findAllByIdsLocked(List.of(10L))).thenReturn(List.of(equipment));


        assertDoesNotThrow(() -> validator.validateReservationAddEquip(dto));
    }
}