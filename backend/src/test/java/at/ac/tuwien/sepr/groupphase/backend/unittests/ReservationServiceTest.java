package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationAddDeleteEquipmentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Pole;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import at.ac.tuwien.sepr.groupphase.backend.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles({"test", "datagenerator"})
@SpringBootTest
public class ReservationServiceTest {
    @Autowired
    ReservationService reservationService;
    @Autowired
    EquipmentService equipmentService;
    @Autowired
    EquipmentRepository equipmentRepository;
    @Autowired
    CustomerProfileRepository customerProfileRepository;
    @Autowired
    CustomerRepository customerRepository;

    private CustomerProfile testCustomerProfile;
    private CustomerProfile testCustomerProfile2;
    private Equipment testEquipment;
    private Equipment testEquipment2;
    private Customer testCustomer;
    @Autowired
    private ReservationRepository reservationRepository;


    @BeforeEach
    public void setup() {
        testCustomer = new Customer("Adrian", "asd", "Adrian", "67", "69", LocalDate.of(1990, 1, 1));
        testCustomer = customerRepository.save(testCustomer);
        testCustomerProfile = new CustomerProfile("Max Mustermann", 180, 67, 33, SkillLevel.BEGINNER, testCustomer);
        testCustomerProfile = customerProfileRepository.save(testCustomerProfile);
        testCustomerProfile2 = new CustomerProfile("Hans hansi", 130, 80, 33, SkillLevel.BEGINNER, testCustomer);
        testCustomerProfile2 = customerProfileRepository.save(testCustomerProfile2);
        testEquipment = new Helmet("Test Helmet Model", 10.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER);
        testEquipment = equipmentRepository.save(testEquipment);
        testEquipment2 = new Pole("ATI", 6.0, 105.0, RentalStatus.FREE, SkillLevel.ADVANCED);
        testEquipment2 = equipmentRepository.save(testEquipment2);
    }

    @Test
    @Transactional
    @Rollback
    public void createReservation_withValidData_returnsSavedReservationDto() {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setCustomerProfileId(testCustomerProfile.getId());
        dto.setEquipmentIds(List.of(testEquipment.getId()));
        dto.setPickUpDate(LocalDate.now().plusDays(2));
        dto.setPickUpTime(LocalTime.of(10, 0));
        dto.setRentDurationDays(3);

        ReservationDetailDto result = reservationService.createReservation(dto);

        assertAll(
            "Verify that the reservation is saved correctly and mapped to DTO",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isNotNull(),
            () -> assertThat(result.getCustomerName()).isEqualTo("Max Mustermann"),
            () -> assertThat(result.getRentDurationDays()).isEqualTo(3),
            () -> assertThat(result.getReturnDate()).isEqualTo(LocalDate.now().plusDays(5))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void createReservation_withUnknownCustomerId_throwsNotFoundException() {
        ReservationCreationDto dto = new ReservationCreationDto();
        dto.setCustomerProfileId(99999L);
        dto.setEquipmentIds(List.of(testEquipment.getId()));
        dto.setPickUpDate(LocalDate.now().plusDays(2));
        dto.setPickUpTime(LocalTime.of(10, 0));
        dto.setRentDurationDays(3);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            reservationService.createReservation(dto)
        );

        assertAll(
            "Verify that the correct exception with the correct message is thrown",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage()).containsIgnoringCase("not found")
        );
    }

    @Test
    @Transactional
    @Rollback
    public void updateReservation_withValidData_returnsUpdatedReservationDto() {

        /*
         * create initial reservation
         */

        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setCustomerProfileId(testCustomerProfile.getId());
        createDto.setEquipmentIds(List.of(testEquipment.getId()));
        createDto.setPickUpDate(LocalDate.now().plusDays(2));
        createDto.setPickUpTime(LocalTime.of(10, 0));
        createDto.setRentDurationDays(3);

        ReservationDetailDto createdReservation =
            reservationService.createReservation(createDto);

        assertThat(createdReservation.getId()).isNotNull();

        Equipment secondEquipment = testEquipment2;
        /*
         * create update dto
         */

        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setId(createdReservation.getId());
        updateDto.setPickUpDate(LocalDate.now().plusDays(5));
        updateDto.setPickUpTime(LocalTime.of(14, 30));
        updateDto.setRentDurationDays(7);
        updateDto.setEquipmentIds(
            List.of(secondEquipment.getId())
        );
        updateDto.setCustomerProfileId(testCustomerProfile2.getId());

        /*
         * execute update
         */

        ReservationDetailDto updatedReservation =
            reservationService.updateReservation(
                updateDto
            );

        /*
         * assertions
         */

        assertAll(
            "Verify that the reservation was updated correctly",

            () -> assertThat(updatedReservation).isNotNull(),

            () -> assertThat(updatedReservation.getId())
                .isEqualTo(createdReservation.getId()),

            () -> assertThat(updatedReservation.getCustomerName())
                .isEqualTo("Hans hansi"),

            () -> assertThat(updatedReservation.getPickUpDate())
                .isEqualTo(LocalDate.now().plusDays(5)),

            () -> assertThat(updatedReservation.getPickUpTime())
                .isEqualTo(LocalTime.of(14, 30)),

            () -> assertThat(updatedReservation.getRentDurationDays())
                .isEqualTo(7),

            () -> assertThat(updatedReservation.getReturnDate())
                .isEqualTo(LocalDate.now().plusDays(12))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void addEquipmentToReservation_withValidData_addsEquipment() {


        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setCustomerProfileId(testCustomerProfile.getId());
        createDto.setEquipmentIds(List.of(testEquipment.getId()));
        createDto.setPickUpDate(LocalDate.now().plusDays(10));
        createDto.setPickUpTime(LocalTime.of(10, 0));
        createDto.setRentDurationDays(5);

        ReservationDetailDto created = reservationService.createReservation(createDto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getItems()).hasSize(1);


        ReservationAddDeleteEquipmentDto addDto = new ReservationAddDeleteEquipmentDto();
        addDto.setId(created.getId());
        addDto.setEquipmentIds(List.of(testEquipment2.getId()));

        ReservationDetailDto result =
            reservationService.addEquipmentToReservation(addDto);

        assertThat(result.getItems()).hasSize(2);

        LocalDate expectedStart = createDto.getPickUpDate();
        LocalDate expectedEnd = expectedStart.plusDays(createDto.getRentDurationDays());


        Equipment updatedEquipment =
            equipmentRepository.findById(testEquipment2.getId()).orElseThrow();

        assertThat(updatedEquipment.getTimePeriodsList())
            .anyMatch(tp ->
                tp.getStartDate().equals(expectedStart) &&
                    tp.getEndDate().equals(expectedEnd) &&
                    tp.getPeriodType() == PeriodType.RENTED
            );

        assertAll(
            "Verify equipment was added successfully",

            () -> assertThat(result).isNotNull(),

            () -> assertThat(result.getId())
                .isEqualTo(created.getId()),

            () -> assertThat(result.getItems())
                .hasSize(2),

            () -> assertThat(
                result.getItems().stream()
                    .anyMatch(e -> e.getId().equals(testEquipment2.getId()))
            ).isTrue()
        );
    }

    @Test
    @Transactional
    @Rollback
    public void deleteReservation_withValidId_removesReservationAndTimePeriods() {

        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setCustomerProfileId(testCustomerProfile.getId());
        createDto.setEquipmentIds(List.of(testEquipment.getId()));
        createDto.setPickUpDate(LocalDate.now().plusDays(10));
        createDto.setPickUpTime(LocalTime.of(10, 0));
        createDto.setRentDurationDays(5);

        ReservationDetailDto created =
            reservationService.createReservation(createDto);

        Long reservationId = created.getId();

        assertThat(reservationId).isNotNull();

        LocalDate expectedStart = createDto.getPickUpDate();
        LocalDate expectedEnd = expectedStart.plusDays(createDto.getRentDurationDays() -1);

        Equipment equipmentBeforeDelete =
            equipmentRepository.findById(testEquipment.getId()).orElseThrow();

        assertThat(equipmentBeforeDelete.getTimePeriodsList())
            .anyMatch(tp ->
                tp.getStartDate().equals(expectedStart) &&
                    tp.getEndDate().equals(expectedEnd)
            );

        reservationService.deleteReservation(reservationId);

        assertThat(reservationRepository.existsById(reservationId)).isFalse();

        Equipment equipmentAfterDelete =
            equipmentRepository.findById(testEquipment.getId()).orElseThrow();

        assertThat(equipmentAfterDelete.getTimePeriodsList())
            .noneMatch(tp ->
                tp.getStartDate().equals(expectedStart) &&
                    tp.getEndDate().equals(expectedEnd) &&
                    tp.getPeriodType() == PeriodType.RENTED
            );
    }

    @Test
    @Transactional
    @Rollback
    public void deleteReservation_withUnknownId_throwsNotFoundException() {

        assertThrows(NotFoundException.class, () ->
            reservationService.deleteReservation(99999L)
        );
    }

    @Test
    @Transactional
    @Rollback
    public void removeEquipmentFromReservation_withValidData_removesEquipmentAndFreesTimePeriod() {
        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setCustomerProfileId(testCustomerProfile.getId());
        createDto.setEquipmentIds(List.of(testEquipment.getId(), testEquipment2.getId()));
        createDto.setPickUpDate(LocalDate.now().plusDays(5));
        createDto.setPickUpTime(LocalTime.of(9, 0));
        createDto.setRentDurationDays(4);

        ReservationDetailDto created = reservationService.createReservation(createDto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getItems()).hasSize(2);

        LocalDate expectedStart = createDto.getPickUpDate();
        LocalDate expectedEnd = expectedStart.plusDays(createDto.getRentDurationDays() -1);

        Equipment equipmentBeforeDelete = equipmentRepository.findById(testEquipment2.getId()).orElseThrow();
        assertThat(equipmentBeforeDelete.getTimePeriodsList())
            .anyMatch(tp ->
                tp.getStartDate().equals(expectedStart) &&
                    tp.getEndDate().equals(expectedEnd) &&
                    tp.getPeriodType() == PeriodType.RENTED
            );

        ReservationAddDeleteEquipmentDto removeDto = new ReservationAddDeleteEquipmentDto();
        removeDto.setId(created.getId());
        removeDto.setEquipmentIds(List.of(testEquipment2.getId()));

        ReservationDetailDto result = reservationService.removeEquipmentFromReservation(removeDto);

        assertAll(
            "Verify equipment was removed successfully",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getId()).isEqualTo(created.getId()),
            () -> assertThat(result.getItems()).hasSize(1), // Nur noch 1 Item übrig
            () -> assertThat(
                result.getItems().stream()
                    .noneMatch(item -> item.getId().equals(testEquipment2.getId()))
            ).as("testEquipment2 should be removed").isTrue(),
            () -> assertThat(
                result.getItems().stream()
                    .anyMatch(item -> item.getId().equals(testEquipment.getId()))
            ).as("testEquipment should still be in the reservation").isTrue()
        );

        Equipment equipmentAfterDelete = equipmentRepository.findById(testEquipment2.getId()).orElseThrow();
        assertThat(equipmentAfterDelete.getTimePeriodsList())
            .noneMatch(tp ->
                tp.getStartDate().equals(expectedStart) &&
                    tp.getEndDate().equals(expectedEnd) &&
                    tp.getPeriodType() == PeriodType.RENTED
            );
    }

    @Test
    @Transactional
    @Rollback
    public void removeEquipmentFromReservation_withUnknownReservationId_throwsNotFoundException() {
        ReservationAddDeleteEquipmentDto removeDto = new ReservationAddDeleteEquipmentDto();
        removeDto.setId(99999L);
        removeDto.setEquipmentIds(List.of(testEquipment.getId()));

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            reservationService.removeEquipmentFromReservation(removeDto)
        );
    }

    @Test
    @Transactional
    @Rollback
    public void searchReservations_withMatchingCriteria_returnsFilteredList() {
        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setCustomerProfileId(testCustomerProfile.getId());
        createDto.setEquipmentIds(List.of(testEquipment.getId()));
        LocalDate searchDate = LocalDate.now().plusDays(15);
        createDto.setPickUpDate(searchDate);
        createDto.setPickUpTime(LocalTime.of(10, 0));
        createDto.setRentDurationDays(3);

        ReservationDetailDto created = reservationService.createReservation(createDto);

        ReservationSearchDto searchDto = new ReservationSearchDto();
        searchDto.setCustomerProfileId(testCustomerProfile.getId());
        searchDto.setPickUpDate(searchDate);
        searchDto.setEquipmentIds(List.of(testEquipment.getId()));

        List<ReservationDetailDto> result = reservationService.searchReservations(searchDto);

        assertAll(
            "Verify that the search returns the correct reservation",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result).isNotEmpty(),
            () -> assertThat(result.stream().anyMatch(res -> res.getId().equals(created.getId()))).isTrue(),
            () -> assertThat(result.get(0).getCustomerName()).isEqualTo("Max Mustermann"),
            () -> assertThat(result.get(0).getItems().stream()
                .anyMatch(item -> item.getId().equals(testEquipment.getId()))).isTrue()
        );
    }

    @Test
    @Transactional
    @Rollback
    public void searchReservations_withNoMatchingCriteria_returnsEmptyList() {
        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setCustomerProfileId(testCustomerProfile.getId());
        createDto.setEquipmentIds(List.of(testEquipment.getId()));
        createDto.setPickUpDate(LocalDate.now().plusDays(5));
        createDto.setPickUpTime(LocalTime.of(10, 0));
        createDto.setRentDurationDays(3);

        reservationService.createReservation(createDto);

        ReservationSearchDto searchDto = new ReservationSearchDto();
        searchDto.setCustomerProfileId(99999L);
        searchDto.setPickUpDate(LocalDate.of(2099, 1, 1));

        List<ReservationDetailDto> result = reservationService.searchReservations(searchDto);

        assertAll(
            "Verify that searching with wrong criteria returns an empty list",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result).isEmpty()
        );
    }

}
