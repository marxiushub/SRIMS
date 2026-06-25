package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
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
import at.ac.tuwien.sepr.groupphase.backend.security.CurrentUserService;
import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import at.ac.tuwien.sepr.groupphase.backend.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

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

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private CurrentUserService currentUserService;

    private Customer testCustomer;
    private CustomerProfile testCustomerProfile;
    private CustomerProfile testCustomerProfile2;
    private Helmet testEquipment;
    private Helmet testEquipment2;

    @BeforeEach
    public void setUp() {
        testCustomer = customerRepository
            .findByEmail("benjamin.marius.widmer@gmail.com")
            .orElseThrow(() -> new IllegalStateException("Test customer not found in DataInitializer!"));

        testCustomerProfile = customerProfileRepository
            .findByCustomerAndProfileName(testCustomer, "Hans")
            .orElseThrow(() -> new IllegalStateException("Profile 'Hans' not found!"));

        testCustomerProfile2 = customerProfileRepository
            .findByCustomerAndProfileName(testCustomer, "Hansine")
            .orElseThrow(() -> new IllegalStateException("Profile 'Hansine' not found!"));

        List<Helmet> testEquipmentList = helmetRepository.findAll();
        if (testEquipmentList.size() < 2) {
            throw new IllegalStateException("At least two helmets are required for ReservationServiceTest");
        }

        testEquipment = testEquipmentList.get(0);
        testEquipment2 = testEquipmentList.get(1);

        when(currentUserService.getUserId()).thenReturn(testCustomer.getId());
        when(currentUserService.hasAuthority("STAFF")).thenReturn(false);
    }

    @AfterEach
    public void cleanupCreatedReservationData() {
        timePeriodsRepository.deleteAll();

        reservationRepository.deleteAll();
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

        double expectedTotalPrice = testEquipment.getPrice() * (3 +1);

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

        double expectedTotalPrice = testEquipment2.getPrice() * (7 +1);

        ReservationDetailDto updatedReservation = reservationService.updateReservationStaff(updateDto, false);

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

        double expectedPriceAfterAdd = (testEquipment.getPrice() + testEquipment2.getPrice()) * (5 +1);

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

        reservationService.deleteReservation(reservationId, true);

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
            reservationService.deleteReservation(99999L, true)
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
        assertThat(created.getTotalPrice()).isEqualTo((testEquipment.getPrice() + testEquipment2.getPrice()) * (3 +1));

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
            () -> assertThat(found.getTotalPrice()).isEqualTo(testEquipment.getPrice() * (3 +1)),
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

    @Test
    void searchReservations_asCustomer_returnsOnlyOwnReservations() {

        ReservationCreationDto dto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(12),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(dto);

        ReservationSearchDto searchDto = new ReservationSearchDto();

        List<ReservationDetailDto> result = reservationService.searchReservations(searchDto);

        assertThat(result)
            .extracting(ReservationDetailDto::getId)
            .contains(created.getId());
    }

    @Test
    void searchReservations_asCustomer_withDifferentAccountId_throwsException() {

        ReservationSearchDto searchDto = new ReservationSearchDto();
        searchDto.setAccountId(999L);

        assertThatThrownBy(() -> reservationService.searchReservations(searchDto))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Cannot search reservations of another customer.");
    }

    @Test
    void searchReservations_asCustomer_ignoresAccountIdAndReturnsOwnOnly() {

        ReservationCreationDto dto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(20),
            LocalDate.now().plusDays(22),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(dto);

        ReservationSearchDto searchDto = new ReservationSearchDto();

        List<ReservationDetailDto> result = reservationService.searchReservations(searchDto);

        assertThat(result)
            .extracting(ReservationDetailDto::getId)
            .contains(created.getId());
    }

    @Test
    void searchReservations_asStaff_returnsAllReservations() {
        when(currentUserService.hasAuthority("STAFF")).thenReturn(true);

        ReservationCreationDto dto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(32),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(dto);

        ReservationSearchDto searchDto = new ReservationSearchDto();

        List<ReservationDetailDto> result = reservationService.searchReservations(searchDto);

        assertThat(result)
            .extracting(ReservationDetailDto::getId)
            .contains(created.getId());
    }

    @Test
    void searchReservations_asStaff_withAccountIdFiltersCorrectly() {
        when(currentUserService.hasAuthority("STAFF")).thenReturn(true);

        ReservationCreationDto dto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(40),
            LocalDate.now().plusDays(42),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(dto);

        ReservationSearchDto searchDto = new ReservationSearchDto();
        searchDto.setAccountId(testCustomer.getId());

        List<ReservationDetailDto> result = reservationService.searchReservations(searchDto);

        assertThat(result)
            .extracting(ReservationDetailDto::getId)
            .contains(created.getId());
    }

    @Test
    void searchReservations_asCustomer_alwaysFiltersToOwnAccountEvenWithoutSettingIt() {
        when(currentUserService.hasAuthority("STAFF")).thenReturn(false);
        when(currentUserService.getUserId()).thenReturn(testCustomer.getId());

        ReservationCreationDto dto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(50),
            LocalDate.now().plusDays(52),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(dto);

        ReservationSearchDto searchDto = new ReservationSearchDto(); // kein accountId gesetzt

        List<ReservationDetailDto> result = reservationService.searchReservations(searchDto);

        assertThat(result)
            .extracting(ReservationDetailDto::getId)
            .contains(created.getId());
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

    @Test
    public void reservationById_asCustomer_withOwnReservation_returnsDto() {
        when(currentUserService.hasAuthority("STAFF")).thenReturn(false);
        when(currentUserService.getUserId()).thenReturn(testCustomer.getId());

        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationDetailDto found = reservationService.reservationById(created.getId());

        assertAll(
            () -> assertThat(found).isNotNull(),
            () -> assertThat(found.getId()).isEqualTo(created.getId()),
            () -> assertThat(found.getCustomerName()).isEqualTo("Hans")
        );
    }

    @Test
    public void reservationById_asCustomer_withForeignReservation_throwsAccessDenied() {
        when(currentUserService.hasAuthority("STAFF")).thenReturn(false);
        when(currentUserService.getUserId()).thenReturn(1L);

        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(createDto);


        when(currentUserService.getUserId()).thenReturn(999L);

        assertThatThrownBy(() ->
            reservationService.reservationById(created.getId())
        )
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("not allowed");
    }

    @Test
    public void reservationById_asStaff_returnsAnyReservation() {
        when(currentUserService.hasAuthority("STAFF")).thenReturn(true);

        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationDetailDto found = reservationService.reservationById(created.getId());

        assertAll(
            () -> assertThat(found).isNotNull(),
            () -> assertThat(found.getId()).isEqualTo(created.getId())
        );
    }

    @Test
    public void reservationById_withNullUserId_throwsAccessDenied() {
        when(currentUserService.hasAuthority("STAFF")).thenReturn(false);
        when(currentUserService.getUserId()).thenReturn(testCustomer.getId());

        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(createDto);

        when(currentUserService.getUserId()).thenReturn(null);

        assertThatThrownBy(() ->
            reservationService.reservationById(created.getId())
        ).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void reservationById_withUnknownId_throwsNotFoundException() {
        when(currentUserService.hasAuthority("STAFF")).thenReturn(true);

        assertThrows(NotFoundException.class, () ->
            reservationService.reservationById(99999L)
        );
    }

    @Test
    public void updateReservation_withOnlyPartialData_updatesOnlyProvidedFieldsAndKeepsRest() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(4),
            LocalTime.of(10, 0)
        );
        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setId(created.getId());
        updateDto.setReservationStatus(ReservationStatus.PICKED_UP);

        ReservationDetailDto updated = reservationService.updateReservationStaff(updateDto, false);

        assertAll(
            "Verify that null fields in DTO don't overwrite existing data",
            () -> assertThat(updated.getReservationStatus()).isEqualTo(ReservationStatus.PICKED_UP),
            () -> assertThat(updated.getStartDate()).isEqualTo(created.getStartDate()),
            () -> assertThat(updated.getEndDate()).isEqualTo(created.getEndDate()),
            () -> assertThat(updated.getPickUpTime()).isEqualTo(created.getPickUpTime()),
            () -> assertThat(updated.getItems()).hasSize(1)
        );
    }

    @Test
    public void searchReservations_withNullDto_asCustomer_returnsOnlyOwnReservations() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(createDto);

        List<ReservationDetailDto> result = reservationService.searchReservations(null);

        assertThat(result)
            .extracting(ReservationDetailDto::getId)
            .contains(created.getId());
    }

    @Test
    public void searchReservations_withNullDto_asStaff_returnsAllReservations() {
        when(currentUserService.hasAuthority("STAFF")).thenReturn(true);

        ReservationCreationDto dto1 = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created = reservationService.createReservation(dto1);

        List<ReservationDetailDto> result = reservationService.searchReservations(null);

        assertThat(result)
            .extracting(ReservationDetailDto::getId)
            .contains(created.getId());
    }

    @Test
    public void searchReservations_withAdvancedFilters_returnsFilteredList() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(10),
            LocalTime.of(14, 0)
        );
        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationSearchDto searchDto = new ReservationSearchDto();
        searchDto.setAccountId(testCustomer.getId());
        searchDto.setPickUpTime(LocalTime.of(14, 0));
        searchDto.setSearchRangeStart(LocalDate.now().plusDays(2));
        searchDto.setSearchRangeEnd(LocalDate.now().plusDays(12));
        searchDto.setEquipmentIds(List.of());

        List<ReservationDetailDto> result = reservationService.searchReservations(searchDto);

        assertAll(
            () -> assertThat(result).isNotEmpty(),
            () -> assertThat(result.stream().anyMatch(r -> r.getId().equals(created.getId()))).isTrue()
        );
    }

    @Test
    public void createReservation_withSameStartAndEndDate_calculatesPriceForOneDay() {
        LocalDate sameDate = LocalDate.now().plusDays(2);

        ReservationCreationDto dto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            sameDate,
            sameDate,
            LocalTime.of(10, 0)
        );

        ReservationDetailDto result = reservationService.createReservation(dto);

        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getTotalPrice()).isEqualTo(testEquipment.getPrice())
        );
    }

    @Test
    public void updateReservation_withOnlyStartDateChanged_updatesSuccessfully() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );
        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setId(created.getId());
        updateDto.setStartDate(LocalDate.now().plusDays(3));

        ReservationDetailDto updated = reservationService.updateReservation(updateDto);

        assertAll(
            "Verify that changing ONLY the start date evaluates the branches correctly",
            () -> assertThat(updated.getStartDate()).isEqualTo(LocalDate.now().plusDays(3)),
            () -> assertThat(updated.getEndDate()).isEqualTo(created.getEndDate()),
            () -> assertThat(updated.getItems()).hasSize(1)
        );
    }

    @Test
    public void updateReservation_withOnlyEndDateChanged_updatesSuccessfully() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );
        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setId(created.getId());
        updateDto.setEndDate(LocalDate.now().plusDays(10));

        ReservationDetailDto updated = reservationService.updateReservation(updateDto);

        assertAll(
            "Verify that changing ONLY the end date evaluates the branches correctly",
            () -> assertThat(updated.getEndDate()).isEqualTo(LocalDate.now().plusDays(10)),
            () -> assertThat(updated.getStartDate()).isEqualTo(created.getStartDate())
        );
    }

    @Test
    public void updateReservation_withNoDatesAndNoEquipmentChanged_updatesSuccessfully() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );
        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setId(created.getId());
        updateDto.setPickUpTime(LocalTime.of(16, 0));

        ReservationDetailDto updated = reservationService.updateReservation(updateDto);

        assertThat(updated.getPickUpTime()).isEqualTo(LocalTime.of(16, 0));
    }

    @Test
    public void deleteReservation_withMixedTimePeriods_removesOnlyMatchingPeriods() {
        Helmet customEquipment = new Helmet("Mixed Period Helmet", 100.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER);

        customEquipment.addTimePeriod(LocalDate.now().plusDays(20), LocalDate.now().plusDays(25), PeriodType.REPAIR, null);
        customEquipment.addTimePeriod(LocalDate.now().plusDays(30), LocalDate.now().plusDays(35), PeriodType.RENTED, null);

        Helmet savedEquipment = helmetRepository.save(customEquipment);

        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(savedEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );
        ReservationDetailDto created = reservationService.createReservation(createDto);

        reservationService.deleteReservation(created.getId(), true);

        boolean maintenanceKept = timePeriodsRepository.findByEquipment(savedEquipment).stream()
            .anyMatch(tp -> tp.getPeriodType() == PeriodType.REPAIR);

        assertThat(maintenanceKept).isTrue();
    }

    @Test
    public void processOverdueReservations_withOverdueItems_sendsEmailAndUpdatesFlag() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );
        ReservationDetailDto created = reservationService.createReservation(createDto);

        Reservation reservation = reservationRepository.findById(created.getId()).orElseThrow();
        reservation.setStartDate(LocalDate.now().minusDays(10));
        reservation.setEndDate(LocalDate.now().minusDays(2));
        reservation.setReservationStatus(ReservationStatus.PICKED_UP);
        reservation.setOverdueReminderSent(false);
        reservationRepository.save(reservation);

        LocalDate boundaryDate = LocalDate.now().minusDays(1);
        reservationService.processOverdueReservations(boundaryDate);

        Reservation updated = reservationRepository.findById(created.getId()).orElseThrow();

        assertAll(
            "Verify that the overdue job correctly updates the flag and calls the email service",
            () -> assertThat(updated.isOverdueReminderSent()).isTrue()
        );

        verify(emailService, times(1)).sendOverdueReminder(anyList(),any(Reservation.class));
    }

    @Test
    public void processPickUpReminders_withUpcomingItems_sendsEmailAndUpdatesFlag() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );
        ReservationDetailDto created = reservationService.createReservation(createDto);

        Reservation reservation = reservationRepository.findById(created.getId()).orElseThrow();
        reservation.setReservationStatus(ReservationStatus.CREATED);
        reservation.setPickUpReminderSent(false);
        reservationRepository.save(reservation);

        reservationService.processPickUpReminders();

        Reservation updated = reservationRepository.findById(created.getId()).orElseThrow();

        assertAll(
            "Verify that the pick-up job correctly updates the flag and calls the email service",
            () -> assertThat(updated.isPickUpReminderSent()).isTrue()
        );

        verify(emailService, times(1)).sendPickUpReminderEmail(anyList(), any(Reservation.class));
    }

    @Test
    public void addEquipmentToReservation_withDuplicateIdsInRequest_throwsValidationException() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            LocalTime.of(10, 0)
        );
        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationAddDeleteEquipmentDto addDto = new ReservationAddDeleteEquipmentDto();
        addDto.setId(created.getId());
        addDto.setEquipmentIds(List.of(testEquipment2.getId(), testEquipment2.getId()));

        ValidationException exception = assertThrows(ValidationException.class, () ->
            reservationService.addEquipmentToReservation(addDto)
        );

        assertAll(
            "Verify that duplicate equipment IDs in the request are blocked",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getErrors().stream()
                .anyMatch(e -> e.contains("duplicate IDs"))).isTrue()
        );
    }

    @Test
    public void addEquipmentToReservation_withEquipmentAlreadyInReservation_throwsValidationException() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            LocalTime.of(10, 0)
        );
        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationAddDeleteEquipmentDto addDto = new ReservationAddDeleteEquipmentDto();
        addDto.setId(created.getId());
        addDto.setEquipmentIds(List.of(testEquipment.getId()));

        ValidationException exception = assertThrows(ValidationException.class, () ->
            reservationService.addEquipmentToReservation(addDto)
        );

        assertAll(
            "Verify that adding already existing equipment is blocked",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getErrors().stream()
                .anyMatch(e -> e.contains("already part of this reservation"))).isTrue()
        );
    }

    @Test
    public void removeEquipmentFromReservation_leavingReservationEmpty_throwsValidationException() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(8),
            LocalTime.of(9, 0)
        );
        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationAddDeleteEquipmentDto removeDto = new ReservationAddDeleteEquipmentDto();
        removeDto.setId(created.getId());
        removeDto.setEquipmentIds(List.of(testEquipment.getId()));

        ValidationException exception = assertThrows(ValidationException.class, () ->
            reservationService.removeEquipmentFromReservation(removeDto)
        );

        assertAll(
            "Verify that a reservation cannot be emptied completely",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getErrors().stream()
                .anyMatch(e -> e.contains("must contain at least one equipment"))).isTrue()
        );
    }

    @Test
    public void removeEquipmentFromReservation_withEquipmentNotInReservation_throwsValidationException() {
        ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId()),
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(8),
            LocalTime.of(9, 0)
        );
        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationAddDeleteEquipmentDto removeDto = new ReservationAddDeleteEquipmentDto();
        removeDto.setId(created.getId());
        removeDto.setEquipmentIds(List.of(testEquipment2.getId()));

        ValidationException exception = assertThrows(ValidationException.class, () ->
            reservationService.removeEquipmentFromReservation(removeDto)
        );

        assertAll(
            "Verify that removing unassociated equipment throws an error",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getErrors().stream()
                .anyMatch(e -> e.contains("not part of this reservation"))).isTrue()
        );
    }

}