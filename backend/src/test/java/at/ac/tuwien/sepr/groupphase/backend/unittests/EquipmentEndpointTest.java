package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SkiCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles({"test", "datagenerator"})
@AutoConfigureMockMvc
@SpringBootTest
public class EquipmentEndpointTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EquipmentService equipmentService;

    @Test
    public void createEquipSevicePosTest() throws Exception{

        String json = """
        {
          "type": "HELMET",
          "price": 199.99,
          "model": "Poc Skull X",
          "status": "FREE",
          "targetSkillLevel": "ADVANCED",
          "size": 58
        }
        """;
        MvcResult result = mockMvc.perform(post("/api/v1/equipment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andReturn();


        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        String responseBody = result.getResponse().getContentAsString();

        assertThat(responseBody).contains("Poc Skull X");
        assertThat(responseBody).contains("199.99");
    }

    @Test
    public void deleteEquipmentPosTest() throws Exception {
        SkiCreationDto dto = new SkiCreationDto();
        dto.setPrice(67);
        dto.setModel("Test Ski für Delete");
        dto.setStatus(RentalStatus.FREE);
        dto.setTargetSkillLevel(SkillLevel.ADVANCED);
        dto.setLength(200);

        Equipment savedEquip = equipmentService.createEquipment(dto);
        Long generatedId = savedEquip.getId();

        MvcResult result = mockMvc.perform(delete("/api/v1/equipment/" + generatedId))
            .andReturn();


        assertThat(result.getResponse().getStatus()).isEqualTo(204);

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
            equipmentService.deleteEquipment(generatedId));
    }

    @Test
    public void deleteEquipmentNegTest() throws Exception {
        Long nonExistentId = 99999L;

        MvcResult result = mockMvc.perform(delete("/api/v1/equipment/" + nonExistentId))
            .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }




}
