package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation.HelmetCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation.SkiCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"test"})
@AutoConfigureMockMvc
@SpringBootTest
public class EquipmentEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    private String adminToken() {
        return jwtTokenizer.getAuthToken(
            ADMIN_USER,
            ID,
            ADMIN_PERMISSIONS
        );
    }

    @Test
    public void createEquipment_withValidDto_returns200AndSavedData() throws Exception {
        String json = """
            {
              "type": "HELMET",
              "price": 199.99,
              "model": "Poc Skull X Endpoint Create",
              "status": "FREE",
              "targetSkillLevel": "ADVANCED",
              "size": 58
            }
            """;

        mockMvc.perform(post("/api/v1/equipment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].model").value("Poc Skull X Endpoint Create"))
            .andExpect(jsonPath("$[0].price").value(199.99))
            .andExpect(jsonPath("$[0].equipmentType").value("HELMET"));
    }

    @Test
    public void getAllEquipment_returnsCreatedEquipment() throws Exception {
        EquipmentDetailDto created = createTestSki("Universal Search Test Ski");

        mockMvc.perform(get("/api/v1/equipment")
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[*].id").value(hasItem(created.getId().intValue())))
            .andExpect(jsonPath("$[*].model").value(hasItem("Universal Search Test Ski")));
    }

    @Test
    public void searchEquipment_withSpecificTypeAndModel_returnsFilteredList() throws Exception {
        createTestSki("Atomic Redster Endpoint Test");
        createTestSki("Fischer Ranger Endpoint Test");

        mockMvc.perform(get("/api/v1/equipment")
                .param("type", "SKI")
                .param("model", "atomic")
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].model").value(hasItem("Atomic Redster Endpoint Test")))
            .andExpect(jsonPath("$[*].model").value(not(hasItem("Fischer Ranger Endpoint Test"))));
    }

    @Test
    public void searchEquipment_withNoMatchingCriteria_returnsEmptyList() throws Exception {
        createTestSki("Universal No Match Test Ski");

        mockMvc.perform(get("/api/v1/equipment")
                .param("model", "THIS_MODEL_SHOULD_NOT_EXIST_999999")
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    public void searchEquipment_withInvalidEnumParameter_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/equipment")
                .param("type", "UFO")
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void getEquipmentByType_withValidType_returnsOnlyEquipmentOfThatType() throws Exception {
        EquipmentDetailDto helmet = createTestHelmet("Poc Type Test Helmet");
        EquipmentDetailDto ski = createTestSki("Atomic Type Test Ski");

        mockMvc.perform(get("/api/v1/equipment/type/helmet")
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[*].id").value(hasItem(helmet.getId().intValue())))
            .andExpect(jsonPath("$[*].id").value(not(hasItem(ski.getId().intValue()))));
    }

    @Test
    public void getEquipmentByType_withUnknownType_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/type/UNKNOWN_NONEXISTENT_TYPE")
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void getEquipmentById_withValidId_returnsEquipmentDetail() throws Exception {
        EquipmentDetailDto created = createTestSki("Atomic GetById Test Ski");

        mockMvc.perform(get("/api/v1/equipment/{id}", created.getId())
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(created.getId()))
            .andExpect(jsonPath("$.model").value("Atomic GetById Test Ski"))
            .andExpect(jsonPath("$.equipmentType").value("SKI"));
    }

    @Test
    public void getEquipmentById_withUnknownId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/{id}", 99999L)
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void getEquipmentByBarcodeId_withValidBarcode_returnsEquipmentDetail() throws Exception {
        EquipmentDetailDto created = createTestSki("Barcode Endpoint Test Ski");

        mockMvc.perform(get("/api/v1/equipment/barcode/{barcodeId}", created.getBarcodeId())
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.barcodeId").value(created.getBarcodeId()))
            .andExpect(jsonPath("$.model").value("Barcode Endpoint Test Ski"))
            .andExpect(jsonPath("$.equipmentType").value("SKI"));
    }

    @Test
    public void getEquipmentByBarcodeId_withUnknownBarcode_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/barcode/{barcodeId}", "NON-EXISTENT-BARCODE-99999")
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateEquipment_withValidSkiDto_returnsOkAndUpdatedFields() throws Exception {
        EquipmentDetailDto created = createTestSki("Atomic Update Test Ski");

        String patchJson = """
            {
              "type": "SKI",
              "price": 149.99,
              "length": 165
            }
            """;

        mockMvc.perform(patch("/api/v1/equipment/{id}", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson)
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(created.getId()))
            .andExpect(jsonPath("$.model").value("Atomic Update Test Ski"))
            .andExpect(jsonPath("$.price").value(149.99))
            .andExpect(jsonPath("$.length").value(165))
            .andExpect(jsonPath("$.status").value("FREE"));
    }

    @Test
    public void updateEquipment_withMismatchedType_returnsBadRequest() throws Exception {
        EquipmentDetailDto created = createTestSki("Type Conflict Test Ski");

        String invalidPatchJson = """
            {
              "type": "HELMET",
              "price": 50.0,
              "size": 58
            }
            """;

        mockMvc.perform(patch("/api/v1/equipment/{id}", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPatchJson)
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void updateEquipment_withUnknownId_returnsNotFound() throws Exception {
        String patchJson = """
            {
              "type": "SKI",
              "price": 120.0,
              "length": 150
            }
            """;

        mockMvc.perform(patch("/api/v1/equipment/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson)
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void deleteEquipment_withValidId_returnsNoContentAndDeletesEntity() throws Exception {
        EquipmentDetailDto created = createTestSki("Delete Endpoint Test Ski");

        mockMvc.perform(delete("/api/v1/equipment/{id}", created.getId())
                .header(securityProperties.getAuthHeader(), adminToken()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/equipment/{id}", created.getId())
                .header(securityProperties.getAuthHeader(), adminToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void deleteEquipment_withUnknownId_returnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/equipment/{id}", 99999L)
                .header(securityProperties.getAuthHeader(), adminToken()))
            .andExpect(status().isNotFound());
    }

    private EquipmentDetailDto createTestSki(String model) {
        SkiCreationDto dto = new SkiCreationDto();
        dto.setPrice(67);
        dto.setModel(model);
        dto.setStatus(RentalStatus.FREE);
        dto.setTargetSkillLevel(SkillLevel.ADVANCED);
        dto.setLength(200);

        List<EquipmentDetailDto> savedEquip = equipmentService.createEquipment(dto);
        return savedEquip.get(0);
    }

    private EquipmentDetailDto createTestHelmet(String model) {
        HelmetCreationDto dto = new HelmetCreationDto();
        dto.setPrice(199.99);
        dto.setModel(model);
        dto.setStatus(RentalStatus.FREE);
        dto.setTargetSkillLevel(SkillLevel.ADVANCED);
        dto.setSize(58);

        List<EquipmentDetailDto> savedEquip = equipmentService.createEquipment(dto);
        return savedEquip.get(0);
    }
}