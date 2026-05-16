package at.ac.tuwien.sepr.groupphase.backend.unittests;

import static org.assertj.core.api.Assertions.assertThat;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SkiCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Ski;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.EquipmentService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "datagenerator"})
@SpringBootTest
public class EquipmentServiceTests {
    @Autowired
    private EquipmentService service;

    @Test
    @Transactional
    @Rollback
    public void equipmentCreationTest(){

        SkiCreationDto dto = new SkiCreationDto();

        dto.setPrice(67);
        dto.setModel("Levono");
        dto.setStatus(RentalStatus.FREE);
        dto.setTargetSkillLevel(SkillLevel.ADVANCED);
        dto.setLength(200);

        Equipment equip = service.createEquipment(dto);

        assertThat(equip != null);
        assertThat(equip instanceof Ski);
        assertThat(equip.getId()).isNotNull();

    }


}
