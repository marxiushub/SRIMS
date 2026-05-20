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
import static org.assertj.core.api.Assertions.fail;
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
    public void createEquipment_withValidDto_returns200AndSavedData() {

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

        try {
            MvcResult result = mockMvc.perform(post("/api/v1/equipment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check if equipment was successfully created",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).contains("Poc Skull X"),
                () -> assertThat(responseBody).contains("199.99")
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception");
        }
    }

    @Test
    public void deleteEquipment_withValidId_returns204AndDeletesEntity() {
        SkiCreationDto dto = new SkiCreationDto();
        dto.setPrice(67);
        dto.setModel("Test Ski für Delete");
        dto.setStatus(RentalStatus.FREE);
        dto.setTargetSkillLevel(SkillLevel.ADVANCED);
        dto.setLength(200);

        Equipment savedEquip = equipmentService.createEquipment(dto);
        Long generatedId = savedEquip.getId();

        try {


            MvcResult result = mockMvc.perform(delete("/api/v1/equipment/" + generatedId))
                .andReturn();


            assertAll(
                "Check that the status is 204 and the element was really deleted from the DB",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(204),
                () -> assertThrows(NotFoundException.class, () -> equipmentService.deleteEquipment(generatedId))
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception");
        }
    }

    @Test
    public void deleteEquipment_withNonExistentId_returns404AndThrowsNotFoundException() {
        long nonExistentId = 99999L;

        try {

            MvcResult result = mockMvc.perform(delete("/api/v1/equipment/" + nonExistentId))
                .andReturn();

            assertThat(result.getResponse().getStatus()).isEqualTo(404);
        } catch (Exception e) {
            fail("Test failed because of unexpected exception");
        }
    }

    @Test
    public void updateEquipment_withValidDto_returns200AndUpdatedFields() {
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

        try {

            MvcResult result = mockMvc.perform(patch("/api/v1/equipment/" + generatedId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check HTTP-Status and whether only the sent fields were changed",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).contains("149.99"),
                () -> assertThat(responseBody).contains("165"),
                () -> assertThat(responseBody).contains("Atomic Ski"),
                () -> assertThat(responseBody).contains("FREE")
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception");
        }
    }

    @Test
    public void updateEquipment_withMismatchedType_returns400AndThrowsIllegalArgumentException() {
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

        try {


            MvcResult result = mockMvc.perform(patch("/api/v1/equipment/" + generatedId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidPatchJson))
                .andReturn();

            Exception resolvedException = result.getResolvedException();

            assertAll(
                "Check that a mismatched type (helmet update on a ski entity) is rejected with 400",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(400),
                () -> assertThat(resolvedException).isNotNull(),
                () -> assertThat(resolvedException).isInstanceOf(IllegalArgumentException.class)
            );


        } catch (Exception e) {
            fail("Test failed because of unexpected exception");
        }
    }

    @Test
    public void updateEquipment_withNonExistentId_returns404AndThrowsNotFoundException() {
        long nonExistentId = 99999L;

        String patchJson = """
            {
              "type": "SKI",
              "price": 120.0,
              "length": 150
            }
            """;

        try {


            MvcResult result = mockMvc.perform(patch("/api/v1/equipment/" + nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andReturn();

            Exception resolvedException = result.getResolvedException();

            assertAll(
                "test, if Update with unknown ID throws NotFoundException",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(404),
                () -> assertThat(resolvedException).isNotNull(),
                () -> assertThat(resolvedException).isInstanceOf(NotFoundException.class)
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception");
        }
    }

    @Test
    public void searchEquipment_withSpecificTypeAndModel_returnsFilteredList() {
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

        try {

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
        } catch (Exception e) {
            fail("Test failed because of unexpected exception");
        }
    }

    @Test
    public void searchEquipment_withoutParameters_returnsAllItems() {
        SkiCreationDto ski = new SkiCreationDto();
        ski.setPrice(100);
        ski.setModel("Universal Ski");
        ski.setStatus(RentalStatus.FREE);
        ski.setTargetSkillLevel(SkillLevel.BEGINNER);
        ski.setLength(160);
        equipmentService.createEquipment(ski);

        try {

            MvcResult result = mockMvc.perform(get("/api/v1/equipment")
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "test if it returns everything without parameters",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).contains("Universal Ski")
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception");
        }
    }

    @Test
    public void searchEquipment_withNoMatchingCriteria_returnsEmptyList() {
        SkiCreationDto ski = new SkiCreationDto();
        ski.setPrice(100);
        ski.setModel("Universal Ski");
        ski.setStatus(RentalStatus.FREE);
        ski.setTargetSkillLevel(SkillLevel.BEGINNER);
        ski.setLength(160);
        equipmentService.createEquipment(ski);

        try {

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
        } catch (Exception e) {
            fail("Test failed because of unexpected exception");
        }
    }

    @Test
    public void searchEquipment_withInvalidEnumParameter_returns400BadRequest() {

        try {


            MvcResult result = mockMvc.perform(get("/api/v1/equipment")
                    .param("type", "UFO")
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn();


            Exception resolvedException = result.getResolvedException();

            assertAll(
                "Check if invalid enum values in the URL parameter are blocked as 400 Bad Request",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(400),
                () -> assertThat(resolvedException).isNotNull(),
                () -> assertThat(resolvedException.getClass().getSimpleName()).contains("MethodArgumentNotValidException"));
        } catch (Exception e) {
            fail("Test failed because of unexpected exception");
        }
    }


}
