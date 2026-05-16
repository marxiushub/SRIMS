package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SkiCreationDto;
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
    void getEquipmentByTypeUnknownTypeThrowsNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            equipmentService.equipmentByType("invalid_type"));

        assertTrue(exception.getMessage().contains("Unknown equipment type"));
    }
}
