package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.HelmetDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.HelmetUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SkiCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SkiUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.EquipmentType;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Ski;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.HelmetRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.EquipmentServiceImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

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
    }

    @Test
    @Transactional
    @Rollback
    public void equipmentCreationTest() {

        SkiCreationDto dto = new SkiCreationDto();

        dto.setPrice(67);
        dto.setModel("Levono");
        dto.setStatus(RentalStatus.FREE);
        dto.setTargetSkillLevel(SkillLevel.ADVANCED);
        dto.setLength(200);

        Equipment equip = equipmentService.createEquipment(dto);

        assertThat(equip != null);
        assertThat(equip instanceof Ski);
        assertThat(equip.getId()).isNotNull();

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
        Long invalidId = -1L;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
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
    @Transactional
    @Rollback
    void updateEquipment_validPartialUpdate_updatesOnlyProvidedFieldsAndReturnsDto() {
        // Arrange: Ein existierendes Equipment in der DB anlegen
        Helmet savedHelmet = helmetRepository.save(
            new Helmet("Poc Skull", 199.99, 58.0, RentalStatus.FREE, SkillLevel.BEGINNER)
        );
        Long id = savedHelmet.getId();

        HelmetUpdateDto updateDto = new HelmetUpdateDto();
        ReflectionTestUtils.setField(updateDto, EquipmentUpdateDto.class, "type", EquipmentType.HELMET, EquipmentType.class);        updateDto.setPrice(150.00);
        updateDto.setSize(60.0);

        EquipmentDetailDto result = equipmentService.updateEquipment(id, updateDto);

        assertThat(result).isNotNull();
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

        assertTrue(exception.getMessage().contains("Type mismatch"),
            "Exception message should indicate type mismatch");
    }


}
