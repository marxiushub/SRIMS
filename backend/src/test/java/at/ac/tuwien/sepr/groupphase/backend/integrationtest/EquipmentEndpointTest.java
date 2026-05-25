package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.detail.EquipmentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.EquipmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "generateData"})
@AutoConfigureMockMvc
public class EquipmentEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipmentService equipmentService;

    @Test
    public void getAllEquipmentWithGeneratedDataReturnsFullList() throws Exception {
        mockMvc.perform(get("/api/v1/equipment")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(90));
    }

    @Test
    public void getEquipmentByTypeValidTypeReturnsSubsetList() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/type/helmet")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    public void getEquipmentByTypeUnknownTypeThrowsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/type/UNKNOWN_NONEXISTENT_TYPE")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void getEquipmentByIdValidIdReturnsEquipmentDetail() throws Exception {
        Long testId = 1L;

        mockMvc.perform(get("/api/v1/equipment/{id}", testId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(testId))
            .andExpect(jsonPath("$.equipmentType").exists())
            .andExpect(jsonPath("$.model").exists());
    }

    @Test
    public void getEquipmentByIdUnknownIdReturnsNotFound() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(get("/api/v1/equipment/{id}", nonExistentId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void getEquipmentByBarcodeIdValidBarcodeReturnsEquipmentDetail() throws Exception {
        at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.search.EquipmentSearchDto search =
            new at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.equipmentdto.search.EquipmentSearchDto();
        List<EquipmentDetailDto> allGenerated = equipmentService.searchEquipment(search);

        String validBarcodeId = allGenerated.getFirst().getBarcodeId();

        mockMvc.perform(get("/api/v1/equipment/barcode/{barcodeId}", validBarcodeId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.barcodeId").value(validBarcodeId))
            .andExpect(jsonPath("$.equipmentType").exists())
            .andExpect(jsonPath("$.model").exists());
    }

    @Test
    public void getEquipmentByBarcodeIdUnknownBarcodeReturnsNotFound() throws Exception {
        String nonExistentBarcode = "NON-EXISTENT-BARCODE-99999";

        mockMvc.perform(get("/api/v1/equipment/barcode/{barcodeId}", nonExistentBarcode)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
