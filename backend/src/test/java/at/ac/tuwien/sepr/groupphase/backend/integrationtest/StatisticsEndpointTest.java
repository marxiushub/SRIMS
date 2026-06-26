package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.IntegrationTestBase;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.datagenerator.DataInitializer;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles({"test", "datagenerator", "generateData"})
@AutoConfigureMockMvc
@SpringBootTest
public class StatisticsEndpointTest extends IntegrationTestBase implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;

    @Autowired
    private DataInitializer initializer;

    @BeforeEach
    public void setup() {

        initializer.initializeData();

        var adminUser = userRepository.findUserByEmail("admin@email.com")
            .orElseThrow();

        adminToken = jwtTokenizer.getAuthToken(
            adminUser.getEmail(),
            adminUser.getId(),
            ADMIN_PERMISSIONS
        );
    }

    @Test
    public void getStatistics_detailDegreeTrue_returnsItemCounts() throws Exception {
        String json = """
            {
              "searchStart": "%s",
              "searchEnd": "%s",
              "detailDegree": true,
              "type": "SKI"
            }
            """.formatted(
            LocalDate.now().minusDays(30),
            LocalDate.now().plusDays(1)
        );

        mockMvc.perform(post("/api/v1/statistics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.detailDegree").value(true))
            .andExpect(jsonPath("$.itemCounts").exists())
            .andExpect(jsonPath("$.modelCounts").isEmpty()); // Da im Service nur itemCounts gesetzt werden
    }

    @Test
    public void getStatistics_detailDegreeFalse_returnsModelCounts() throws Exception {
        String json = """
            {
              "searchStart": "%s",
              "searchEnd": "%s",
              "detailDegree": false,
              "type": "SKI"
            }
            """.formatted(
            LocalDate.now().minusDays(30),
            LocalDate.now().plusDays(1)
        );

        mockMvc.perform(post("/api/v1/statistics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.detailDegree").value(false))
            .andExpect(jsonPath("$.modelCounts").exists())
            .andExpect(jsonPath("$.itemCounts").isEmpty());
    }

    @Test
    public void getStatistics_invalidDates_returns400() throws Exception {
        // Testet die @NotNull Validierung der DTOs
        String json = """
            {
              "searchStart": null,
              "searchEnd": null,
              "detailDegree": false
            }
            """;

        mockMvc.perform(post("/api/v1/statistics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), adminToken))
            .andExpect(status().isBadRequest());
    }
}