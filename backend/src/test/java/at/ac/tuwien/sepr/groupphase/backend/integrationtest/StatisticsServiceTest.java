package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.datagenerator.DataInitializer;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.statistics.StatisticsRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.statistics.StatisticsResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.HelmetRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ReservationService;
import at.ac.tuwien.sepr.groupphase.backend.service.StatisicsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;

@ActiveProfiles({"test", "datagenerator", "generateData"})
@SpringBootTest
public class StatisticsServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private HelmetRepository helmetRepository;

    @Autowired
    private StatisicsService statisicsService;

    private CustomerProfile testCustomerProfile;
    private Customer testCustomer;
    private CustomerProfile testCustomerProfile2;
    private Helmet testEquipment;
    private Helmet testEquipment2;


    @BeforeEach
    public void setup() {
        dataInitializer.initializeData();

        testCustomer = customerRepository
            .findByEmail("benjamin.marius.widmer@gmail.com")
            .orElseThrow(() -> new IllegalStateException("Test-Customer not found in DataInitializer!"));

        testCustomerProfile = customerProfileRepository
            .findByCustomerAndProfileName(testCustomer, "Hans")
            .orElseThrow(() -> new IllegalStateException("Profil 'Hans' not found"));

        testCustomerProfile2 = customerProfileRepository
            .findByCustomerAndProfileName(testCustomer, "Hansine")
            .orElseThrow(() -> new IllegalStateException("Profil 'Hansine' not found"));

        testEquipment = helmetRepository.save(
            new Helmet("Statistics Test Helmet", 10.0, 56.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE));
        testEquipment2 = helmetRepository.save(
            new Helmet("Statistics Test Helmet", 10.0, 58.0, RentalStatus.FREE, SkillLevel.INTERMEDIATE));

    }

    @Test
    public void getEquipmentStatistics_withMatchingReservation_returnsCorrectItemCount() {

       ReservationCreationDto createDto = createReservationCreationDto(
            testCustomerProfile.getId(),
            List.of(testEquipment.getId(), testEquipment2.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5),
            LocalTime.of(10, 0)
        );

        ReservationDetailDto created =
            reservationService.createReservation(createDto);

        ReservationUpdateDto updateDto = new ReservationUpdateDto();
        updateDto.setId(created.getId());
        updateDto.setReservationStatus(ReservationStatus.PICKED_UP);

        reservationService.updateReservation(updateDto);

        StatisticsRequestDto request = new StatisticsRequestDto();
        request.setSearchStart(LocalDate.now());
        request.setSearchEnd(LocalDate.now().plusDays(10));
        request.setDetailDegree(true);

        StatisticsResponseDto resultHighDetail =
            statisicsService.getEquipmentStatistics(request);

        request.setDetailDegree(false);

        StatisticsResponseDto resultLowDetail =
            statisicsService.getEquipmentStatistics(request);

        assertAll("High Detail Statistics",
            () -> org.junit.jupiter.api.Assertions.assertNotNull(resultHighDetail),
            () -> org.junit.jupiter.api.Assertions.assertTrue(resultHighDetail.getDetailDegree()),
            () -> org.junit.jupiter.api.Assertions.assertNull(resultHighDetail.getModelCounts(), "Model counts should be null when high detail is requested"),
            () -> org.junit.jupiter.api.Assertions.assertNotNull(resultHighDetail.getItemCounts()),
            () -> org.junit.jupiter.api.Assertions.assertEquals(2, resultHighDetail.getItemCounts().size(), "Should contain exactly 2 distinct items"),
            () -> org.junit.jupiter.api.Assertions.assertEquals(4, resultHighDetail.getItemCounts().get(testEquipment.getId()), "First helmet should have 4 rental days"),
            () -> org.junit.jupiter.api.Assertions.assertEquals(4, resultHighDetail.getItemCounts().get(testEquipment2.getId()), "Second helmet should have 4 rental days")
        );

        // Assertions für Low Detail
        assertAll("Low Detail Statistics",
            () -> org.junit.jupiter.api.Assertions.assertNotNull(resultLowDetail),
            () -> org.junit.jupiter.api.Assertions.assertFalse(resultLowDetail.getDetailDegree()),
            () -> org.junit.jupiter.api.Assertions.assertNull(resultLowDetail.getItemCounts(), "Item counts should be null when low detail is requested"),
            () -> org.junit.jupiter.api.Assertions.assertNotNull(resultLowDetail.getModelCounts()),
            () -> org.junit.jupiter.api.Assertions.assertEquals(1, resultLowDetail.getModelCounts().size(), "Should contain exactly 1 model entry since both helmets share the same model name"),
            () -> org.junit.jupiter.api.Assertions.assertEquals(8, resultLowDetail.getModelCounts().get(testEquipment.getModel()), "The shared model should accumulate to 8 total rental days (4 + 4)")
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
