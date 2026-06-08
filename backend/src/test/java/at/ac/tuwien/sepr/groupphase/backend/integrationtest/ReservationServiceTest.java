package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TimePeriodsRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.HelmetRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles({"test", "datagenerator", "generateData"})
@SpringBootTest
public class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private HelmetRepository helmetRepository;

    @Autowired
    private TimePeriodsRepository timePeriodsRepository;

    private Customer testCustomer;
    private CustomerProfile testCustomerProfile;
    private CustomerProfile testCustomerProfile2;
    private Helmet testEquipment;
    private Helmet testEquipment2;

    @BeforeEach
    public void setUp() {
        testCustomer = customerRepository
            .findByEmail("hans.hansinger@email.com")
            .orElseThrow(() -> new IllegalStateException("Test-Customer nicht im DataInitializer gefunden!"));

        testCustomerProfile = customerProfileRepository
            .findByCustomerAndProfileName(testCustomer, "Hans")
            .orElseThrow(() -> new IllegalStateException("Profil 'Hans' nicht gefunden!"));

        testCustomerProfile2 = customerProfileRepository
            .findByCustomerAndProfileName(testCustomer, "Hansine")
            .orElseThrow(() -> new IllegalStateException("Profil 'Hansine' nicht gefunden!"));

        List<Helmet> testEquipmentList = helmetRepository.findAll();
        if (testEquipmentList.size() < 2) {
            throw new IllegalStateException("At least two helmets are required for ReservationServiceTest");
        }

        testEquipment = testEquipmentList.get(0);
        testEquipment2 = testEquipmentList.get(1);
    }

    @AfterEach
    public void cleanupCreatedReservationData() {
        reservationRepository.findAll().forEach(reservation ->
            reservationService.deleteReservation(reservation.getId())
        );

        timePeriodsRepository.deleteAllInBatch();
    }

    @Test
    public void createReservation_withValidData_returnsSavedReservationDto() {
        ReservationCreationDto dto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );

        double expectedTotalPrice = testEquipment.getPrice() * 3;

        ReservationDetailDto result = reservationService.createReservation(dto);

        assertAll(
            "Verify that the reservation is saved correctly and mapped to DTO",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isNotNull(),
            () -> assertThat(result.getCustomerName()).isEqualTo("Hans"),
            () -> assertThat(result.getStartDate()).isEqualTo(LocalDate.now().plusDays(2)),
            () -> assertThat(result.getEndDate()).isEqualTo(LocalDate.now().plusDays(5)),
            () -> assertThat(result.getReservationStatus()).isEqualTo(ReservationStatus.CREATED),
            () -> assertThat(result.getTotalPrice()).isEqualTo(expectedTotalPrice)
        );
    }

    @Test
    public void createReservation_withUnknownCustomerProfileId_throwsValidationException() {
        ReservationCreationDto dto = createReservationCreationDto(
            99999L,
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(3),
            LocalTime.of(10, 0)
        );

        ValidationException exception = assertThrows(ValidationException.class, () ->
            reservationService.createReservation(dto)
        );

        assertAll(
            "Verify that the correct exception with the correct message is thrown",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage()).containsIgnoringCase("Validation failed"),
            () -> assertThat(exception.getErrors().stream()
                .anyMatch(error -> error.contains("No such CustomerProfile"))).isTrue()
        );
    }

    @Test
    public void createReservation_withoutReservationStatus_throwsValidationException() {
        ReservationCreationDto dto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );
        dto.setReservationStatus(null);

        ValidationException exception = assertThrows(ValidationException.class, () ->
            reservationService.createReservation(dto)
        );

        assertAll(
            "Verify that missing reservation status is rejected",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getErrors().stream()
                .anyMatch(error -> error.contains("Reservation status must not be null"))).isTrue()
        );
    }

    @Test
    public void updateReservation_withValidData_returnsUpdatedReservationDto() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(3),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto createdReservation = reservationService.createReservation(createDto);
        assertThat(createdReservation.getId()).isNotNull();

        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setId(createdReservation.getId());
        updateDto.setStartDate(LocalDate.now().plusDays(5));
        updateDto.setEndDate(LocalDate.now().plusDays(12));
        updateDto.setPickUpTime(LocalTime.of(14, 30));
        updateDto.setEquipmentIds(List.of(testEquipment2.getId()));
        updateDto.setCustomerProfileId(testCustomerProfile2.getId());
        updateDto.setReservationStatus(ReservationStatus.PICKED_UP);

        double expectedTotalPrice = testEquipment2.getPrice() * 7;

        ReservationDetailDto updatedReservation = reservationService.updateReservation(updateDto);

        assertAll(
            "Verify that the reservation was updated correctly",
            () -> assertThat(updatedReservation).isNotNull(),
            () -> assertThat(updatedReservation.getId()).isEqualTo(createdReservation.getId()),
            () -> assertThat(updatedReservation.getCustomerName()).isEqualTo("Hansine"),
            () -> assertThat(updatedReservation.getStartDate()).isEqualTo(LocalDate.now().plusDays(5)),
            () -> assertThat(updatedReservation.getPickUpTime()).isEqualTo(LocalTime.of(14, 30)),
            () -> assertThat(updatedReservation.getEndDate()).isEqualTo(LocalDate.now().plusDays(12)),
            () -> assertThat(updatedReservation.getReservationStatus()).isEqualTo(ReservationStatus.PICKED_UP),
            () -> assertThat(updatedReservation.getTotalPrice()).isEqualTo(expectedTotalPrice)
        );
    }

    @Test
    public void addEquipmentToReservation_withValidData_addsEquipment() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(createDto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getItems()).hasSize(1);

        double expectedPriceAfterAdd = (testEquipment.getPrice() + testEquipment2.getPrice()) * 5;

        ReservationAddDeleteEquipmentDto addDto = new ReservationAddDeleteEquipmentDto();
        addDto.setId(created.getId());
        addDto.setEquipmentIds(List.of(testEquipment2.getId()));

        ReservationDetailDto result = reservationService.addEquipmentToReservation(addDto);

        LocalDate expectedStart = createDto.getStartDate();
        LocalDate expectedEnd = createDto.getEndDate();

        assertThat(timePeriodsRepository.findByEquipment(testEquipment2))
            .anyMatch(tp ->
                tp.getStartDate().equals(expectedStart)
                    && tp.getEndDate().equals(expectedEnd)
                    && tp.getPeriodType() == PeriodType.RENTED
            );

        assertAll(
            "Verify equipment was added successfully",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(created.getId()),
            () -> assertThat(result.getItems()).hasSize(2),
            () -> assertThat(result.getReservationStatus()).isEqualTo(ReservationStatus.CREATED),
            () -> assertThat(result.getTotalPrice()).isEqualTo(expectedPriceAfterAdd),
            () -> assertThat(result.getItems().stream()
                .anyMatch(e -> e.getId().equals(testEquipment2.getId()))).isTrue()
        );
    }

    @Test
    public void deleteReservation_withValidId_removesReservationAndTimePeriods() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(createDto);
        Long reservationId = created.getId();

        assertThat(reservationId).isNotNull();

        LocalDate expectedStart = createDto.getStartDate();
        LocalDate expectedEnd = createDto.getEndDate();

        assertThat(timePeriodsRepository.findByEquipment(testEquipment))
            .anyMatch(tp ->
                tp.getStartDate().equals(expectedStart)
                    && tp.getEndDate().equals(expectedEnd)
            );

        reservationService.deleteReservation(reservationId);

        assertThat(reservationRepository.existsById(reservationId)).isFalse();

        assertThat(timePeriodsRepository.findByEquipment(testEquipment))
            .noneMatch(tp ->
                tp.getStartDate().equals(expectedStart)
                    && tp.getEndDate().equals(expectedEnd)
                    && tp.getPeriodType() == PeriodType.RENTED
            );
    }

    @Test
    public void deleteReservation_withUnknownId_throwsNotFoundException() {
        assertThrows(NotFoundException.class, () ->
            reservationService.deleteReservation(99999L)
        );
    }

    @Test
    public void removeEquipmentFromReservation_withValidData_removesEquipmentAndFreesTimePeriod() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId(), testEquipment2.getId()),
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(8),
            LocalTime.of(9, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(createDto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getItems()).hasSize(2);
        assertThat(created.getTotalPrice()).isEqualTo((testEquipment.getPrice() + testEquipment2.getPrice()) * 3);

        LocalDate expectedStart = createDto.getStartDate();
        LocalDate expectedEnd = createDto.getEndDate();

        assertThat(timePeriodsRepository.findByEquipment(testEquipment2))
            .anyMatch(tp ->
                tp.getStartDate().equals(expectedStart)
                    && tp.getEndDate().equals(expectedEnd)
                    && tp.getPeriodType() == PeriodType.RENTED
            );

        ReservationAddDeleteEquipmentDto removeDto = new ReservationAddDeleteEquipmentDto();
        removeDto.setId(created.getId());
        removeDto.setEquipmentIds(List.of(testEquipment2.getId()));

        ReservationDetailDto result = reservationService.removeEquipmentFromReservation(removeDto);

        assertAll(
            "Verify equipment was removed successfully",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(created.getId()),
            () -> assertThat(result.getItems()).hasSize(1),
            () -> assertThat(result.getReservationStatus()).isEqualTo(ReservationStatus.CREATED),
            () -> assertThat(result.getItems().stream()
                .noneMatch(item -> item.getId().equals(testEquipment2.getId()))).isTrue(),
            () -> assertThat(result.getItems().stream()
                .anyMatch(item -> item.getId().equals(testEquipment.getId()))).isTrue()
        );

        assertThat(timePeriodsRepository.findByEquipment(testEquipment2))
            .noneMatch(tp ->
                tp.getStartDate().equals(expectedStart)
                    && tp.getEndDate().equals(expectedEnd)
                    && tp.getPeriodType() == PeriodType.RENTED
            );
    }

    @Test
    public void removeEquipmentFromReservation_withUnknownReservationId_throwsNotFoundException() {
        ReservationAddDeleteEquipmentDto removeDto = new ReservationAddDeleteEquipmentDto();
        removeDto.setId(99999L);
        removeDto.setEquipmentIds(List.of(testEquipment.getId()));

        assertThrows(NotFoundException.class, () ->
            reservationService.removeEquipmentFromReservation(removeDto)
        );
    }

    @Test
    public void searchReservations_withMatchingCriteria_returnsFilteredList() {
        LocalDate searchDate = LocalDate.now().plusDays(15);

        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            searchDate,
            searchDate.plusDays(3),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationSearchDto searchDto = new ReservationSearchDto();
        searchDto.setCustomerProfileId(testCustomerProfile.getId());
        searchDto.setStartDate(searchDate);
        searchDto.setEquipmentIds(List.of(testEquipment.getId()));
        searchDto.setReservationStatus(ReservationStatus.CREATED);

        List<ReservationDetailDto> result = reservationService.searchReservations(searchDto);

        ReservationDetailDto found = result.stream()
            .filter(res -> res.getId().equals(created.getId()))
            .findFirst()
            .orElseThrow();

        assertAll(
            "Verify that the search returns the correct reservation",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result).isNotEmpty(),
            () -> assertThat(found.getCustomerName()).isEqualTo("Hans"),
            () -> assertThat(found.getStartDate()).isEqualTo(searchDate),
            () -> assertThat(found.getReservationStatus()).isEqualTo(ReservationStatus.CREATED),
            () -> assertThat(found.getTotalPrice()).isEqualTo(testEquipment.getPrice() * 3),
            () -> assertThat(found.getItems().stream()
                .anyMatch(item -> item.getId().equals(testEquipment.getId()))).isTrue()
        );
    }

    @Test
    public void searchReservations_withNoMatchingCriteria_returnsEmptyList() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(8),
            LocalTime.of(10, 0)
        );

        reservationService.createReservation(createDto);

        ReservationSearchDto searchDto = new ReservationSearchDto();
        searchDto.setCustomerProfileId(99999L);
        searchDto.setStartDate(LocalDate.of(2099, 1, 1));
        searchDto.setReservationStatus(ReservationStatus.CREATED);

        List<ReservationDetailDto> result = reservationService.searchReservations(searchDto);

        assertAll(
            "Verify that searching with wrong criteria returns an empty list",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result).isEmpty()
        );
    }

    private ReservationCreationDto createReservationCreationDto(
        Long customerProfileId,
        List<Long> equipmentIds,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime pickUpTime
    ) {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setCustomerProfileId(customerProfileId);
        dto.setEquipmentIds(equipmentIds);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setPickUpTime(pickUpTime);
        dto.setReservationStatus(ReservationStatus.CREATED);
        return dto;
    }
}