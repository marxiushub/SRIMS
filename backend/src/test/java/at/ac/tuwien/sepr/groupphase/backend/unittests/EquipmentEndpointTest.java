package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.SkiCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    public void createEquipServicePosTest() throws Exception{

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

       assertThrows(NotFoundException.class, () ->
            equipmentService.deleteEquipment(generatedId));
    }

    @Test
    public void deleteEquipmentNegTest() throws Exception {
        long nonExistentId = 99999L;

        MvcResult result = mockMvc.perform(delete("/api/v1/equipment/" + nonExistentId))
            .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    public void updateEquipmentPosTest() throws Exception {
        SkiCreationDto dto = new SkiCreationDto();
        dto.setPrice(100);
        dto.setModel("Atomic Ski");
        dto.setStatus(RentalStatus.FREE);
        dto.setTargetSkillLevel(SkillLevel.BEGINNER);
        dto.setLength(160);

        Equipment savedEquip = equipmentService.createEquipment(dto);
        Long generatedId = savedEquip.getId();


        String patchJson = """
        {
          "type": "SKI",
          "price": 149.99,
          "length": 165
        }
        """;


        MvcResult result = mockMvc.perform(patch("/api/v1/equipment/" + generatedId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
            .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        String responseBody = result.getResponse().getContentAsString();

        assertThat(responseBody).contains("149.99");
        assertThat(responseBody).contains("165");

        assertThat(responseBody).contains("Atomic Ski");
        assertThat(responseBody).contains("FREE");
    }

    @Test
    public void updateEquipmentTypeMismatchNegTest() throws Exception {
        SkiCreationDto dto = new SkiCreationDto();
        dto.setPrice(80);
        dto.setModel("Type-Conflict Ski");
        dto.setStatus(RentalStatus.FREE);
        dto.setTargetSkillLevel(SkillLevel.BEGINNER);
        dto.setLength(170);

        Equipment savedEquip = equipmentService.createEquipment(dto);
        Long generatedId = savedEquip.getId();

        String invalidPatchJson = """
        {
          "type": "HELMET",
          "price": 50.0,
          "size": 58
        }
        """;

        MvcResult result = mockMvc.perform(patch("/api/v1/equipment/" + generatedId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPatchJson))
            .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    public void updateEquipmentNotFoundNegTest() throws Exception {
        long nonExistentId = 99999L;

        String patchJson = """
        {
          "type": "SKI",
          "price": 120.0,
          "length": 150
        }
        """;

        MvcResult result = mockMvc.perform(patch("/api/v1/equipment/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
            .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    public void searchEquipment_withSpecificTypeAndModel_returnsFilteredList() throws Exception {
        SkiCreationDto ski1 = new SkiCreationDto();
        ski1.setPrice(100);
        ski1.setModel("Atomic Redster");
        ski1.setStatus(RentalStatus.FREE);
        ski1.setTargetSkillLevel(SkillLevel.ADVANCED);
        ski1.setLength(170);
        equipmentService.createEquipment(ski1);

        SkiCreationDto ski2 = new SkiCreationDto();
        ski2.setPrice(80);
        ski2.setModel("Fischer Ranger");
        ski2.setStatus(RentalStatus.FREE);
        ski2.setTargetSkillLevel(SkillLevel.BEGINNER);
        ski2.setLength(160);
        equipmentService.createEquipment(ski2);

        MvcResult result = mockMvc.perform(get("/api/v1/equipment")
                .param("type", "SKI")
                .param("model", "atomic")
                .contentType(MediaType.APPLICATION_JSON))
            .andReturn();


        String responseBody = result.getResponse().getContentAsString();

        assertAll(
            "test if only the ski with the correct model was found",
            () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
            () -> assertThat(responseBody).contains("Atomic Redster"),
            () -> assertThat(responseBody).doesNotContain("Fischer Ranger")
        );
    }

    @Test
    public void searchEquipment_withoutParameters_returnsAllItems() throws Exception {
        SkiCreationDto ski = new SkiCreationDto();
        ski.setPrice(100);
        ski.setModel("Universal Ski");
        ski.setStatus(RentalStatus.FREE);
        ski.setTargetSkillLevel(SkillLevel.BEGINNER);
        ski.setLength(160);
        equipmentService.createEquipment(ski);


        MvcResult result = mockMvc.perform(get("/api/v1/equipment")
                .contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        assertAll(
            "test if it returns everything without parameters",
            () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
            () -> assertThat(responseBody).contains("Universal Ski")
        );
    }

    @Test
    public void searchEquipment_withNoMatchingCriteria_returnsEmptyList() throws Exception {
        // Arrange (Given)
        SkiCreationDto ski = new SkiCreationDto();
        ski.setPrice(100);
        ski.setModel("Universal Ski");
        ski.setStatus(RentalStatus.FREE);
        ski.setTargetSkillLevel(SkillLevel.BEGINNER);
        ski.setLength(160);
        equipmentService.createEquipment(ski);

        MvcResult result = mockMvc.perform(get("/api/v1/equipment")
                .param("type", "SNOWBOARD")
                .contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        assertAll(
            "Check that an empty search is correctly processed as an empty JSON array",
            () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
            () -> assertThat(responseBody).isEqualTo("[]"),
            () -> assertThat(responseBody).doesNotContain("Universal Ski")
        );
    }

    @Test
    public void searchEquipment_withInvalidEnumParameter_returns400BadRequest() throws Exception {


        MvcResult result = mockMvc.perform(get("/api/v1/equipment")
                .param("type", "UFO")
                .contentType(MediaType.APPLICATION_JSON))
            .andReturn();


        Exception resolvedException = result.getResolvedException();

        assertAll(
            "Check if invalid enum values in the URL parameter are blocked as 400 Bad Request",
            () -> assertThat(result.getResponse().getStatus()).isEqualTo(400),
            () -> assertThat(resolvedException).isNotNull(),
            () -> assertThat(resolvedException.getClass().getSimpleName()).contains("MethodArgumentNotValidException")        );
    }




}
