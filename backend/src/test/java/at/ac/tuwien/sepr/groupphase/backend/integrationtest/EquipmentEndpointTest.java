package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

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
        mockMvc.perform(get("/api/v1/equipment/helmet")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    public void getEquipmentByTypeUnknownTypeThrowsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/equipment/UNKNOWN_NONEXISTENT_TYPE")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
