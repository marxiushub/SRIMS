package at.ac.tuwien.sepr.groupphase.backend.unittests;

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




}
