package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation.SkiCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.HelmetDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.SkiDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.overview.EquipmentStatusOverviewDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.search.EquipmentSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update.EquipmentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update.HelmetUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.update.SkiUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.PeriodType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Ski;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.LocalizedError;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.HelmetRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.SkiRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.EquipmentServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.EquipmentValidator;
import jakarta.transaction.Transactional;
import org.h2.mvstore.db.RowDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

    @Autowired
    private EquipmentRepository equipmentRepository;


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
    void deleteEquipmentValidIdDeletesSuccessfully() {
        //Creting new Helmet which is in no reservation
        Helmet savedHelmet = helmetRepository.save(
            new Helmet(
                "Test Helmet Model",
                10.0,
                55.0,
                RentalStatus.FREE,
                SkillLevel.BEGINNER
            )
        );

        Long validId = savedHelmet.getId();

        assertTrue(
            helmetRepository.findById(validId).isPresent(),
            "Helmet should exist"
        );

        equipmentService.deleteEquipment(validId);

        assertTrue(
            helmetRepository.findById(validId).isEmpty(),
            "Helmet should not exist after deletion"
        );
    }

    @Test
    void deleteEquipmentInvalidIdThrowsIllegalArgumentException() {
        long invalidId = -1L;
        assertThrows(IllegalArgumentException.class, () ->
            equipmentService.deleteEquipment(invalidId)
        );

    }


    @Test
    void getEquipmentByTypeUnknownTypeThrowsNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            equipmentService.equipmentByType("invalid_type"));

        assertTrue(exception.getMessage().contains("Unknown equipment type"));
    }

    @Test
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
    public void getEquipmentByIdUnknownIdThrowsNotFoundException() {
        Long invalidId = 99999L;

        assertThrows(NotFoundException.class, () -> equipmentService.equipmentById(invalidId));
    }

    @Test
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
    public void getEquipmentByNonexistentBarcodeIdThrowsNotFoundException() {
        String invalidBarcodeId = "invalid_barcode_id";

        assertThrows(NotFoundException.class, () -> equipmentService.equipmentByBarcodeId(invalidBarcodeId));
    }

    @Test
    public void getEquipmentByNullOrEmptyBarcodeIdThrowsIllegalArgumentException() {
        assertAll(
            "Verify that null or empty barcode strings throw IllegalArgumentException",
            () -> assertThrows(IllegalArgumentException.class, () -> equipmentService.equipmentByBarcodeId(null)),
            () -> assertThrows(IllegalArgumentException.class, () -> equipmentService.equipmentByBarcodeId(""))
        );
    }

    @Test
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
    void searchEquipment_withNoMatchingCriteria_returnsEmptyList() {
        helmetRepository.save(new Helmet("Standard Helmet", 80.0, 54.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        EquipmentSearchDto searchDto = new EquipmentSearchDto();
        searchDto.setType(EquipmentType.SKI);
        searchDto.setModel("Ghost Ski");

        List<EquipmentDetailDto> result = equipmentService.searchEquipment(searchDto);

        assertAll(
            () -> assertThat(result).isEmpty()
        );
    }

    @Test
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


    @Test
    void updateEquipmentStatuses_toRented_fromFree_updatesSuccessfully() {
        Helmet helmet = helmetRepository.save(
            new Helmet("Status Helmet", 100.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        equipmentService.updateEquipmentStatuses(List.of(helmet.getId()), RentalStatus.RENTED);

        Equipment updated = equipmentRepository.findById(helmet.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(RentalStatus.RENTED);
    }

    @Test
    void updateEquipmentStatuses_toMaintenance_fromFree_updatesSuccessfully() {
        Helmet helmet = helmetRepository.save(
            new Helmet("Maint Helmet", 100.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        equipmentService.updateEquipmentStatuses(List.of(helmet.getId()), RentalStatus.MAINTENANCE);

        Equipment updated = equipmentRepository.findById(helmet.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(RentalStatus.MAINTENANCE);
    }

    @Test
    void updateEquipmentStatuses_toFree_fromRented_updatesSuccessfully() {
        Helmet helmet = helmetRepository.save(
            new Helmet("FreeAgain Helmet", 100.0, 55.0, RentalStatus.RENTED, SkillLevel.BEGINNER));

        equipmentService.updateEquipmentStatuses(List.of(helmet.getId()), RentalStatus.FREE);

        Equipment updated = equipmentRepository.findById(helmet.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(RentalStatus.FREE);
    }

    @Test
    void updateEquipmentStatuses_toFree_whenAlreadyFree_throwsIllegalArgument() {
        Helmet helmet = helmetRepository.save(
            new Helmet("AlreadyFree Helmet", 100.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            equipmentService.updateEquipmentStatuses(List.of(helmet.getId()), RentalStatus.FREE));

        assertThat(ex.getMessage()).contains("cannot be updated");
    }

    @Test
    void updateEquipmentStatuses_toRented_whenNotFree_throwsIllegalArgument() {
        Helmet helmet = helmetRepository.save(
            new Helmet("Busy Helmet", 100.0, 55.0, RentalStatus.MAINTENANCE, SkillLevel.BEGINNER));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            equipmentService.updateEquipmentStatuses(List.of(helmet.getId()), RentalStatus.RENTED));

        assertThat(ex.getMessage()).contains("cannot be updated");
    }

    @Test
    void updateEquipmentStatuses_withNullOrEmptyIds_throwsIllegalArgument() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () ->
                equipmentService.updateEquipmentStatuses(null, RentalStatus.FREE)),
            () -> assertThrows(IllegalArgumentException.class, () ->
                equipmentService.updateEquipmentStatuses(List.of(), RentalStatus.FREE))
        );
    }

    @Test
    void updateEquipmentStatuses_withNullStatus_throwsIllegalArgument() {
        Helmet helmet = helmetRepository.save(
            new Helmet("NullStatus Helmet", 100.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        assertThrows(IllegalArgumentException.class, () ->
            equipmentService.updateEquipmentStatuses(List.of(helmet.getId()), null));
    }

    @Test
    void updateEquipmentStatuses_withUnknownId_throwsNotFound() {
        assertThrows(NotFoundException.class, () ->
            equipmentService.updateEquipmentStatuses(List.of(99999L), RentalStatus.RENTED));
    }


    @Test
    void getEquipmentById_withNegativeId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            equipmentService.equipmentById(-1L));
    }

    @Test
    void getEquipmentById_withNullId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
            equipmentService.equipmentById(null));
    }


    @Test
    void updateEquipment_withNegativeId_throwsIllegalArgument() {
        HelmetUpdateDto dto = new HelmetUpdateDto();
        ReflectionTestUtils.setField(dto, EquipmentUpdateDto.class, "type", EquipmentType.HELMET, EquipmentType.class);
        assertThrows(IllegalArgumentException.class, () ->
            equipmentService.updateEquipment(-1L, dto));
    }

    @Test
    void updateEquipment_withUnknownId_throwsNotFound() {
        HelmetUpdateDto dto = new HelmetUpdateDto();
        ReflectionTestUtils.setField(dto, EquipmentUpdateDto.class, "type", EquipmentType.HELMET, EquipmentType.class);
        assertThrows(NotFoundException.class, () ->
            equipmentService.updateEquipment(99999L, dto));
    }


    @Test
    void searchEquipment_withStatusFilter_returnsOnlyMatchingStatus() {
        helmetRepository.save(new Helmet("Free One", 80.0, 54.0, RentalStatus.FREE, SkillLevel.BEGINNER));
        helmetRepository.save(new Helmet("Rented One", 80.0, 54.0, RentalStatus.RENTED, SkillLevel.BEGINNER));

        EquipmentSearchDto dto = new EquipmentSearchDto();
        dto.setStatus(RentalStatus.RENTED);

        List<EquipmentDetailDto> result = equipmentService.searchEquipment(dto);

        assertAll(
            () -> assertThat(result).extracting(EquipmentDetailDto::getModel).contains("Rented One"),
            () -> assertThat(result).extracting(EquipmentDetailDto::getModel).doesNotContain("Free One")
        );
    }

    @Test
    void searchEquipment_withSkillLevelFilter_returnsOnlyMatching() {
        helmetRepository.save(new Helmet("Beginner Gear", 80.0, 54.0, RentalStatus.FREE, SkillLevel.BEGINNER));
        helmetRepository.save(new Helmet("Advanced Gear", 80.0, 54.0, RentalStatus.FREE, SkillLevel.ADVANCED));

        EquipmentSearchDto dto = new EquipmentSearchDto();
        dto.setTargetSkillLevel(SkillLevel.ADVANCED);

        List<EquipmentDetailDto> result = equipmentService.searchEquipment(dto);

        assertAll(
            () -> assertThat(result).extracting(EquipmentDetailDto::getModel).contains("Advanced Gear"),
            () -> assertThat(result).extracting(EquipmentDetailDto::getModel).doesNotContain("Beginner Gear")
        );
    }

    @ParameterizedTest
    @EnumSource(EquipmentType.class)
    void searchEquipment_withEachType_filtersCorrectly(EquipmentType type) {
        EquipmentSearchDto dto = new EquipmentSearchDto();
        dto.setType(type);

        List<EquipmentDetailDto> result = equipmentService.searchEquipment(dto);

        assertThat(result).isNotNull();
    }

    @Test
    void searchEquipment_withOnlyStartDate_ignoresDateFilter() {
        helmetRepository.save(new Helmet("Date Helmet", 80.0, 54.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        EquipmentSearchDto dto = new EquipmentSearchDto();
        dto.setStart(LocalDate.now());

        List<EquipmentDetailDto> result = equipmentService.searchEquipment(dto);

        assertThat(result).isNotNull();
    }

    @Test
    void searchEquipment_withTimePeriodOutsideSearchRange_equipmentStaysAvailable() {
        Helmet helmet = new Helmet("Past Booking Helmet", 80.0, 54.0, RentalStatus.FREE, SkillLevel.BEGINNER);
        helmet.addTimePeriod(
            LocalDate.now().minusDays(30),
            LocalDate.now().minusDays(25),
            PeriodType.RENTED,
            null
        );
        helmetRepository.save(helmet);

        EquipmentSearchDto dto = new EquipmentSearchDto();
        dto.setStart(LocalDate.now());
        dto.setEnd(LocalDate.now().plusDays(3));

        List<EquipmentDetailDto> result = equipmentService.searchEquipment(dto);

        assertThat(result).extracting(EquipmentDetailDto::getModel).contains("Past Booking Helmet");
    }

    @Test
    void searchEquipment_withBlankModel_ignoresModelFilter() {
        helmetRepository.save(new Helmet("Some Helmet", 80.0, 54.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        EquipmentSearchDto dto = new EquipmentSearchDto();
        dto.setModel("   ");

        List<EquipmentDetailDto> result = equipmentService.searchEquipment(dto);

        assertThat(result).isNotNull();
    }

    @Test
    void getStatusOverview_returnsAllSixTypesWithAllThreeStatusKeys() {
        EquipmentStatusOverviewDto result = equipmentService.getStatusOverview();

        assertAll(
            "All 6 equipment types and all 3 status keys must be present,"
                + " regardless of the actual DB content",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getCounts()).isNotNull(),
            () -> assertThat(result.getCounts()).containsKeys(EquipmentType.values()),
            () -> assertThat(result.getCounts().get(EquipmentType.HELMET))
                .containsKeys(RentalStatus.values()),
            () -> assertThat(result.getCounts().get(EquipmentType.SKI))
                .containsKeys(RentalStatus.values())
        );
    }

    @Test
    void getStatusOverview_afterSavingTwoFreeHelmets_increasesHelmetFreeCountByTwo() {
        long freeHelmetCountBefore = equipmentService.getStatusOverview()
            .getCounts().get(EquipmentType.HELMET).get(RentalStatus.FREE);

        helmetRepository.save(
            new Helmet("Overview Test Helmet A", 50.0, 56.0, RentalStatus.FREE, SkillLevel.BEGINNER));
        helmetRepository.save(
            new Helmet("Overview Test Helmet B", 50.0, 57.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        long freeHelmetCountAfter = equipmentService.getStatusOverview()
            .getCounts().get(EquipmentType.HELMET).get(RentalStatus.FREE);

        assertThat(freeHelmetCountAfter).isEqualTo(freeHelmetCountBefore + 2);
    }

    @Test
    void getStatusOverview_afterChangingHelmetStatus_movesCountBetweenStatuses() {
        Helmet helmet = helmetRepository.save(
            new Helmet("Overview Status Change Helmet", 50.0, 56.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        long freeBefore = equipmentService.getStatusOverview()
            .getCounts().get(EquipmentType.HELMET).get(RentalStatus.FREE);
        long maintenanceBefore = equipmentService.getStatusOverview()
            .getCounts().get(EquipmentType.HELMET).get(RentalStatus.MAINTENANCE);

        equipmentService.updateEquipmentStatuses(List.of(helmet.getId()), RentalStatus.MAINTENANCE);

        EquipmentStatusOverviewDto after = equipmentService.getStatusOverview();
        long freeAfter = after.getCounts().get(EquipmentType.HELMET).get(RentalStatus.FREE);
        long maintenanceAfter = after.getCounts().get(EquipmentType.HELMET).get(RentalStatus.MAINTENANCE);

        assertAll(
            "A helmet that changes from FREE to MAINTENANCE must move between"
                + " categories in the overview, not be counted in both",
            () -> assertThat(freeAfter).isEqualTo(freeBefore - 1),
            () -> assertThat(maintenanceAfter).isEqualTo(maintenanceBefore + 1)
        );
    }

    @Test
    void getStatusOverview_doesNotMixUpDifferentEquipmentTypes() {
        // ensure a newly created helmet is NOT counted under SKI
        long skiCountBefore = equipmentService.getStatusOverview()
            .getCounts().get(EquipmentType.SKI).get(RentalStatus.FREE);

        helmetRepository.save(
            new Helmet("Type Isolation Test Helmet", 50.0, 56.0, RentalStatus.FREE, SkillLevel.BEGINNER));

        long skiCountAfter = equipmentService.getStatusOverview()
            .getCounts().get(EquipmentType.SKI).get(RentalStatus.FREE);

        assertThat(skiCountAfter).isEqualTo(skiCountBefore);
    }


    //Validator Tests
    @Test
    void validateCreate_invalidPriceAndModel_andCreationNumber_throwsValidationException() {
        EquipmentValidator validator = new EquipmentValidator();

        ValidationException ex = assertThrows(ValidationException.class, () ->
            validator.validateCreate(
                -10.0,
                "   ",
                null,
                200
            )
        );

        assertThat(ex.getErrors()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void validateCreate_modelTooLong_throwsValidationException() {
        EquipmentValidator validator = new EquipmentValidator();

        String longModel = "x".repeat(101);

        ValidationException ex = assertThrows(ValidationException.class, () ->
            validator.validateCreate(
                10.0,
                longModel,
                RentalStatus.FREE,
                1
            )
        );

        assertThat(ex.getErrors())
            .extracting(LocalizedError::message)
            .anyMatch(message -> message.contains("Model must not exceed"));
    }

    @Test
    void validateUpdate_withBlockingReservation_throwsValidationException() {
        EquipmentValidator validator = new EquipmentValidator();

        Helmet helmet = new Helmet("Test", 10.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER);

        // erzeugt "blocking reservation"
        helmet.addTimePeriod(
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(5),
            PeriodType.RENTED,
            null
        );

        ValidationException ex = assertThrows(ValidationException.class, () ->
            validator.validateUpdate(
                helmet,
                20.0,
                "New Model",
                RentalStatus.FREE
            )
        );

        assertThat(ex.getErrors())
            .extracting(LocalizedError::message)
            .contains("Equipment is currently reserved and cannot be updated.");
    }

    @Test
    void validateUpdate_negativePrice_andBlankModel_collectsErrors() {
        EquipmentValidator validator = new EquipmentValidator();

        Helmet helmet = new Helmet("Test", 10.0, 55.0, RentalStatus.FREE, SkillLevel.BEGINNER);

        ValidationException ex = assertThrows(ValidationException.class, () ->
            validator.validateUpdate(
                helmet,
                -5.0,
                "   ",
                RentalStatus.FREE
            )
        );

        assertThat(ex.getErrors()).hasSize(2);
    }

    @Test
    void validateDeletable_withRentedStatus_andBlockingReservation_throwsValidationException() {
        EquipmentValidator validator = new EquipmentValidator();

        Helmet helmet = new Helmet("Test", 10.0, 55.0, RentalStatus.RENTED, SkillLevel.BEGINNER);

        helmet.addTimePeriod(
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(3),
            PeriodType.RENTED,
            null
        );

        ValidationException ex = assertThrows(ValidationException.class, () ->
            validator.validateDeletable(helmet)
        );

        assertThat(ex.getErrors()).hasSize(2);
    }

    @Test
    void validateDeletable_nullEquipment_throwsIllegalArgument() {
        EquipmentValidator validator = new EquipmentValidator();

        assertThrows(IllegalArgumentException.class, () ->
            validator.validateDeletable(null)
        );
    }

}
