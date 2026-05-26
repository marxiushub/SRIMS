package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation.SkiCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.creation.HelmetCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
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
            mockMvc.perform(post("/api/v1/equipment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].model").value("Poc Skull X"))
                .andExpect(jsonPath("$[0].price").value(199.99))
                .andExpect(jsonPath("$[0].equipmentType").value("HELMET"));
        } catch (Exception e) {
            fail("Creating valid helmet equipment failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void getAllEquipment_returnsCreatedEquipment() {
        EquipmentDetailDto created = createTestSki("Universal Search Test Ski");

        try {
            mockMvc.perform(get("/api/v1/equipment")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id").value(hasItem(created.getId().intValue())))
                .andExpect(jsonPath("$[*].model").value(hasItem("Universal Search Test Ski")));
        } catch (Exception e) {
            fail("Searching equipment without parameters failed unexpectedly: " + e.getMessage());
        }

    }

    @Test
    public void searchEquipment_withSpecificTypeAndModel_returnsFilteredList() {
        createTestSki("Atomic Redster Endpoint Test");
        createTestSki("Fischer Ranger Endpoint Test");

        try {
            mockMvc.perform(get("/api/v1/equipment")
                    .param("type", "SKI")
                    .param("model", "atomic")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].model").value(hasItem("Atomic Redster Endpoint Test")))
                .andExpect(jsonPath("$[*].model").value(not(hasItem("Fischer Ranger Endpoint Test"))));
        } catch (Exception e) {
            fail("Searching equipment with type and model failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void searchEquipment_withNoMatchingCriteria_returnsEmptyList() {
        createTestSki("Universal No Match Test Ski");

        try {
            mockMvc.perform(get("/api/v1/equipment")
                    .param("type", "SNOWBOARD")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        } catch (Exception e) {
            fail("Searching equipment with no matching criteria failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void searchEquipment_withInvalidEnumParameter_returnsBadRequest() {
        try {
            mockMvc.perform(get("/api/v1/equipment")
                    .param("type", "UFO")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail("Searching equipment with invalid enum parameter failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void getEquipmentByType_withValidType_returnsOnlyEquipmentOfThatType() {
        EquipmentDetailDto helmet = createTestHelmet("Poc Type Test Helmet");
        EquipmentDetailDto ski = createTestSki("Atomic Type Test Ski");

        try {
            mockMvc.perform(get("/api/v1/equipment/type/helmet")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id").value(hasItem(helmet.getId().intValue())))
                .andExpect(jsonPath("$[*].id").value(not(hasItem(ski.getId().intValue()))));
        } catch (Exception e) {
            fail("Getting equipment by valid type failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void getEquipmentByType_withUnknownType_returnsNotFound() {
        try {
            mockMvc.perform(get("/api/v1/equipment/type/UNKNOWN_NONEXISTENT_TYPE")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        } catch (Exception e) {
            fail("Getting equipment by unknown type failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void getEquipmentById_withValidId_returnsEquipmentDetail() {
        EquipmentDetailDto created = createTestSki("Atomic GetById Test Ski");

        try {
            mockMvc.perform(get("/api/v1/equipment/{id}", created.getId())
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.model").value("Atomic GetById Test Ski"))
                .andExpect(jsonPath("$.equipmentType").value("SKI"));
        } catch (Exception e) {
            fail("Getting equipment by valid ID failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void getEquipmentById_withUnknownId_returnsNotFound() {
        try {
            mockMvc.perform(get("/api/v1/equipment/{id}", 99999L)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        } catch (Exception e) {
            fail("Getting equipment by unknown ID failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void getEquipmentByBarcodeId_withValidBarcode_returnsEquipmentDetail() {
        EquipmentDetailDto created = createTestSki("Barcode Endpoint Test Ski");

        try {
            mockMvc.perform(get("/api/v1/equipment/barcode/{barcodeId}", created.getBarcodeId())
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.barcodeId").value(created.getBarcodeId()))
                .andExpect(jsonPath("$.model").value("Barcode Endpoint Test Ski"))
                .andExpect(jsonPath("$.equipmentType").value("SKI"));
        } catch (Exception e) {
            fail("Getting equipment by valid barcode ID failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void getEquipmentByBarcodeId_withUnknownBarcode_returnsNotFound() {
        try {
            mockMvc.perform(get("/api/v1/equipment/barcode/{barcodeId}", "NON-EXISTENT-BARCODE-99999")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        } catch (Exception e) {
            fail("Getting equipment by unknown barcode ID failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void updateEquipment_withValidSkiDto_returnsOkAndUpdatedFields() {
        EquipmentDetailDto created = createTestSki("Atomic Update Test Ski");

        String patchJson = """
            {
              "type": "SKI",
              "price": 149.99,
              "length": 165
            }
            """;

        try {
            mockMvc.perform(patch("/api/v1/equipment/{id}", created.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.model").value("Atomic Update Test Ski"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.length").value(165))
                .andExpect(jsonPath("$.status").value("FREE"));
        } catch (Exception e) {
            fail("Updating equipment with valid ski DTO failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void updateEquipment_withMismatchedType_returnsBadRequest() {
        EquipmentDetailDto created = createTestSki("Type Conflict Test Ski");

        String invalidPatchJson = """
            {
              "type": "HELMET",
              "price": 50.0,
              "size": 58
            }
            """;

        try {
            mockMvc.perform(patch("/api/v1/equipment/{id}", created.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidPatchJson)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        } catch (Exception e) {
            fail("Updating equipment with mismatched type failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void updateEquipment_withUnknownId_returnsNotFound() {
        String patchJson = """
            {
              "type": "SKI",
              "price": 120.0,
              "length": 150
            }
            """;

        try {
            mockMvc.perform(patch("/api/v1/equipment/{id}", 99999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        } catch (Exception e) {
            fail("Updating equipment with unknown ID failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void deleteEquipment_withValidId_returnsNoContentAndDeletesEntity() {
        EquipmentDetailDto created = createTestSki("Delete Endpoint Test Ski");

        try {
            mockMvc.perform(delete("/api/v1/equipment/{id}", created.getId()))
                .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/equipment/{id}", created.getId())
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        } catch (Exception e) {
            fail("Deleting equipment with valid ID failed unexpectedly: " + e.getMessage());
        }
    }

    @Test
    public void deleteEquipment_withUnknownId_returnsNotFound() {
        try {
            mockMvc.perform(delete("/api/v1/equipment/{id}", 99999L))
                .andExpect(status().isNotFound());
        } catch (Exception e) {
            fail("Deleting equipment with unknown ID failed unexpectedly: " + e.getMessage());
        }
    }

    /*
    Helpers for creating test equipment
     */
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
