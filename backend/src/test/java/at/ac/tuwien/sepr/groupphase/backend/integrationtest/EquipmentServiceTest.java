package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation.SkiCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.HelmetDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.SkiDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.search.EquipmentSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update.EquipmentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update.HelmetUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update.SkiUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.HelmetRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.EquipmentServiceImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles({"test", "datagenerator"})
@SpringBootTest
public class EquipmentServiceTest {

    @Autowired
    private HelmetRepository helmetRepository;

    @Autowired
    private EquipmentServiceImpl equipmentService;

    private Helmet testEquipment;

    @BeforeEach
    public void setup() {
        testEquipment = new Helmet("Test Helmet Model", 10.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER);
        testEquipment.addTimePeriod(
            LocalDate.now(),
            LocalDate.now().plusDays(5),
            PeriodType.RENTED,
            null
        );
    }

    @Test
    @Transactional
    @Rollback
    public void createEquipment_withValidDto_returnsSavedEquipmentWithId() {

        SkiCreationDto dto = new SkiCreationDto();

        dto.setPrice(67);
        dto.setModel("Levono");
        dto.setStatus(RentalStatus.FREE);
        dto.setTargetSkillLevel(SkillLevel.ADVANCED);
        dto.setLength(200);
        dto.setCreationNumber(3);

        List<EquipmentDetailDto> equip = equipmentService.createEquipment(dto);

        assertAll(
            "Verify that the equipment is saved correctly and assigned an ID",
            () -> assertThat(equip).isNotNull(),
            () -> assertThat(equip).hasSize(3),

            () -> assertThat(equip).allMatch(e -> e instanceof SkiDetailDto),
            () -> assertThat(equip).extracting(EquipmentDetailDto::getId).doesNotContainNull()
        );

    }

    @Test
    @Transactional
    @Rollback
    void getEquipmentByTypeValidTypeReturnsMappedList() {
        helmetRepository.save(testEquipment);
        List<EquipmentDetailDto> result = equipmentService.equipmentByType("helmet");

        assertAll(
            () -> assertFalse(result.isEmpty(), "Result list should not be empty"),
            () -> assertTrue(result.stream().anyMatch(dto -> "Test Helmet Model".equals(dto.getModel())),
                "The recently saved test helmet should be in the returned list")
        );
    }

    @Test
    @Transactional
    @Rollback
    void deleteEquipmentValidIdDeletesSuccessfully() {
        Helmet savedHelmet = helmetRepository.save(testEquipment);
        Long validId = savedHelmet.getId();

        assertTrue(helmetRepository.findById(validId).isPresent(), "Helmet should exist");

        equipmentService.deleteEquipment(validId);

        assertTrue(helmetRepository.findById(validId).isEmpty(), "Helmet should not exist after deletion");
    }

    @Test
    @Transactional
    @Rollback
    void deleteEquipmentInvalidIdThrowsIllegalArgumentException() {
        long invalidId = -1L;
        assertThrows(IllegalArgumentException.class, () ->
            equipmentService.deleteEquipment(invalidId)
        );

    }


    @Test
    @Transactional
    @Rollback
    void getEquipmentByTypeUnknownTypeThrowsNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            equipmentService.equipmentByType("invalid_type"));

        assertTrue(exception.getMessage().contains("Unknown equipment type"));
    }

    @Test
    @Transactional
    @Rollback
    public void getEquipmentByIdValidIdReturnsCorrectDto() {
        Helmet savedHelmet = helmetRepository.save(testEquipment);
        Long validId = savedHelmet.getId();

        EquipmentDetailDto result = equipmentService.equipmentById(validId);

        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getModel()).isEqualTo(testEquipment.getModel()),
            () -> assertThat(result.getOccupancy()).as("Occupancy list should not be null").isNotNull(),
            () -> assertThat(result.getOccupancy()).as("Occupancy list should contain exactly 1 element").hasSize(1),
            () -> assertThat(result.getOccupancy().getFirst().getPeriodType()).as("Period type should match").isEqualTo(PeriodType.RENTED),
            () -> assertThat(result.getOccupancy().getFirst().getStartDate()).as("Start date should match").isEqualTo(LocalDate.now()),
            () -> assertThat(result.getOccupancy().getFirst().getEndDate()).as("End date should match").isEqualTo(LocalDate.now().plusDays(5))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void getEquipmentByIdUnknownIdThrowsNotFoundException() {
        Long invalidId = 99999L;

        assertThrows(NotFoundException.class, () -> equipmentService.equipmentById(invalidId));
    }

    @Test
    @Transactional
    @Rollback
    public void getEquipmentByValidBarcodeIdReturnsCorrectDto() {
        Helmet savedHelmet = helmetRepository.save(testEquipment);
        String validBarcodeId = savedHelmet.getBarcodeId();

        EquipmentDetailDto result = equipmentService.equipmentByBarcodeId(validBarcodeId);

        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getModel()).isEqualTo(testEquipment.getModel()),
            () -> assertThat(result.getOccupancy()).as("Occupancy list should not be null").isNotNull(),
            () -> assertThat(result.getOccupancy()).as("Occupancy list should contain exactly 1 element").hasSize(1),
            () -> assertThat(result.getOccupancy().getFirst().getPeriodType()).as("Period type should match").isEqualTo(PeriodType.RENTED),
            () -> assertThat(result.getOccupancy().getFirst().getStartDate()).as("Start date should match").isEqualTo(LocalDate.now()),
            () -> assertThat(result.getOccupancy().getFirst().getEndDate()).as("End date should match").isEqualTo(LocalDate.now().plusDays(5))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void getEquipmentByNonexistentBarcodeIdThrowsNotFoundException() {
        String invalidBarcodeId = "invalid_barcode_id";

        assertThrows(NotFoundException.class, () -> equipmentService.equipmentByBarcodeId(invalidBarcodeId));
    }

    @Test
    @Transactional
    @Rollback
    public void getEquipmentByNullOrEmptyBarcodeIdThrowsIllegalArgumentException() {
        assertAll(
            "Verify that null or empty barcode strings throw IllegalArgumentException",
            () -> assertThrows(IllegalArgumentException.class, () -> equipmentService.equipmentByBarcodeId(null)),
            () -> assertThrows(IllegalArgumentException.class, () -> equipmentService.equipmentByBarcodeId(""))
        );
    }

    @Test
    @Transactional
    @Rollback
    void updateEquipment_validPartialUpdate_updatesOnlyProvidedFieldsAndReturnsDto() {
        Helmet savedHelmet = helmetRepository.save(
            new Helmet("Poc Skull", 199.99, 58.0, RentalStatus.FREE, SkillLevel.BEGINNER)
        );
        Long id = savedHelmet.getId();

        HelmetUpdateDto updateDto = new HelmetUpdateDto();
        ReflectionTestUtils.setField(updateDto, EquipmentUpdateDto.class, "type", EquipmentType.HELMET, EquipmentType.class);
        updateDto.setPrice(150.00);
        updateDto.setSize(60.0);

        EquipmentDetailDto result = equipmentService.updateEquipment(id, updateDto);

        assertThat(result).isInstanceOf(HelmetDetailDto.class);

        HelmetDetailDto helmetResult = (HelmetDetailDto) result;

        assertAll(
            () -> assertThat(helmetResult.getPrice()).isEqualTo(150.00),
            () -> assertThat(helmetResult.getSize()).isEqualTo(60.0),
            () -> assertThat(helmetResult.getModel()).isEqualTo("Poc Skull"),
            () -> assertThat(helmetResult.getStatus()).isEqualTo(RentalStatus.FREE)
        );
    }

    @Test
    @Transactional
    @Rollback
    void updateEquipment_typeMismatch_throwsIllegalArgumentException() {
        Helmet savedHelmet = helmetRepository.save(
            new Helmet("Test Helmet", 99.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER)
        );
        Long helmetId = savedHelmet.getId();

        SkiUpdateDto mismatchDto = new SkiUpdateDto();
        ReflectionTestUtils.setField(mismatchDto, EquipmentUpdateDto.class, "type", EquipmentType.SKI, EquipmentType.class);
        mismatchDto.setLength(170.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            equipmentService.updateEquipment(helmetId, mismatchDto)
        );

        assertAll(
            "Verify the exception details when updating a helmet with ski data",
            () -> assertThat(exception).isNotNull(),
            () -> assertThat(exception.getMessage()).containsIgnoringCase("Type mismatch")
        );
    }

    @Test
    @Transactional
    @Rollback
    void searchEquipment_withSpecificTypeAndModel_returnsFilteredList() {
        helmetRepository.save(new Helmet("UniqueAtomic Redster Helmet", 120.0, 58.0, RentalStatus.FREE, SkillLevel.ADVANCED));
        helmetRepository.save(new Helmet("Fischer Ranger Helmet", 100.0, 56.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        EquipmentSearchDto searchDto = new EquipmentSearchDto();
        searchDto.setType(EquipmentType.HELMET);
        searchDto.setModel("UniqueAtomic");

        List<EquipmentDetailDto> result = equipmentService.searchEquipment(searchDto);

        assertAll(
            "Check that only the correct helmet is found",
            () -> assertThat(result).isNotEmpty(),
            () -> assertThat(result.size()).isEqualTo(1),
            () -> assertThat(result.getFirst().getModel()).isEqualTo("UniqueAtomic Redster Helmet")
        );
    }

    @Test
    @Transactional
    @Rollback
    void searchEquipment_withNullDto_returnsAllItemsSafely() {
        helmetRepository.save(new Helmet("Universal Helmet", 80.0, 54.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        List<EquipmentDetailDto> result = equipmentService.searchEquipment(null);

        assertAll(
            "Check if an empty/null DTO is treated as 'find all'",
            () -> assertThat(result).isNotEmpty(),
            () -> assertTrue(result.stream().anyMatch(dto -> dto.getModel().equals("Universal Helmet")))
        );
    }

    @Test
    @Transactional
    @Rollback
    void searchEquipment_withNoMatchingCriteria_returnsEmptyList() {
        helmetRepository.save(new Helmet("Standard Helmet", 80.0, 54.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        EquipmentSearchDto searchDto = new EquipmentSearchDto();
        searchDto.setType(EquipmentType.SKI);
        searchDto.setModel("Ghost Ski");

        List<EquipmentDetailDto> result = equipmentService.searchEquipment(searchDto);

        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void searchEquipment_withAvailabilityFilter_returnsOnlyFreeEquipment() {

        Helmet freeHelmet = helmetRepository.save(
            new Helmet("Free Helmet", 100.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER)
        );

        Helmet blockedHelmet = new Helmet(
            "Blocked Helmet", 120.0, 56.0, RentalStatus.FREE, SkillLevel.BEGINNER
        );

        blockedHelmet.addTimePeriod(
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(5),
            PeriodType.RENTED,
            null
        );

        helmetRepository.save(blockedHelmet);

        // Search Zeitraum überschneidet blockedHelmet
        EquipmentSearchDto searchDto = new EquipmentSearchDto();
        searchDto.setStart(LocalDate.now());
        searchDto.setEnd(LocalDate.now().plusDays(3));

        List<EquipmentDetailDto> result = equipmentService.searchEquipment(searchDto);


        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result).extracting(EquipmentDetailDto::getModel)
                .contains("Free Helmet"),
            () -> assertThat(result).extracting(EquipmentDetailDto::getModel)
                .doesNotContain("Blocked Helmet")
        );
    }

}
