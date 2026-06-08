package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.IntegrationTestBase;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Ski;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
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
public class ReservationEndpointTest extends IntegrationTestBase implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    private Customer testCustomer;
    private CustomerProfile testProfile;
    private Equipment testEquipment1;
    private Equipment testEquipment2;

    private String userToken() {
        return jwtTokenizer.getAuthToken(
            testCustomer.getEmail(),
            testCustomer.getId(),
            USER_PERMISSIONS
        );
    }

    @BeforeEach
    public void setup() {
        String uniqueSuffix = UUID.randomUUID().toString();

        testCustomer = new Customer(
            "reservation_test_user_" + uniqueSuffix,
            "hashedPassword",
            "reservation.test.user." + uniqueSuffix + "@example.com",
            Set.of(),
            Set.of(),
            "Test",
            "User",
            LocalDate.of(1990, 1, 1)
        );
        testCustomer = customerRepository.save(testCustomer);

        testProfile = new CustomerProfile(
            "Max Mustermann",
            180,
            75,
            42,
            SkillLevel.ADVANCED,
            testCustomer
        );
        testProfile = customerProfileRepository.save(testProfile);

        testEquipment1 = new Helmet(
            "Test Helmet",
            15.0,
            58.0,
            RentalStatus.FREE,
            SkillLevel.BEGINNER
        );
        testEquipment1 = equipmentRepository.save(testEquipment1);

        testEquipment2 = new Ski(
            "Test Ski",
            50.0,
            170.0,
            RentalStatus.FREE,
            SkillLevel.ADVANCED
        );
        testEquipment2 = equipmentRepository.save(testEquipment2);
    }

    @Test
    public void createReservation_withValidDto_returns200AndSavedData() throws Exception {
        String json = """
            {
              "customerProfileId": %d,
              "equipmentIds": [%d],
              "startDate": "%s",
              "endDate": "%s",
              "pickUpTime": "10:00:00",
              "reservationStatus": "CREATED"
            }
            """.formatted(
            testProfile.getId(),
            testEquipment1.getId(),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5)
        );

        mockMvc.perform(post("/api/v1/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), userToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Max Mustermann")))
            .andExpect(content().string(containsString("Test Helmet")))
            .andExpect(jsonPath("$.reservationStatus").value("CREATED"));
    }

    @Test
    public void updateReservation_withValidDto_returns200AndUpdatedFields() throws Exception {
        ReservationDetailDto created = createTestReservation(
            List.of(testEquipment1.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5)
        );

        LocalDate updatedStartDate = LocalDate.now().plusDays(10);
        LocalDate updatedEndDate = LocalDate.now().plusDays(15);

        String patchJson = """
            {
              "id": %d,
              "customerProfileId": %d,
              "equipmentIds": [%d],
              "startDate": "%s",
              "endDate": "%s",
              "pickUpTime": "10:00:00",
              "reservationStatus": "PICKED_UP"
            }
            """.formatted(
            created.getId(),
            testProfile.getId(),
            testEquipment1.getId(),
            updatedStartDate,
            updatedEndDate
        );

        mockMvc.perform(patch("/api/v1/reservation/{id}", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson)
                .header(securityProperties.getAuthHeader(), userToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(created.getId()))
            .andExpect(jsonPath("$.startDate").value(updatedStartDate.toString()))
            .andExpect(jsonPath("$.endDate").value(updatedEndDate.toString()))
            .andExpect(jsonPath("$.reservationStatus").value("PICKED_UP"));
    }

    @Test
    public void removeEquipmentFromReservation_withValidData_returns200AndRemovesItem() throws Exception {
        ReservationDetailDto created = createTestReservation(
            List.of(testEquipment1.getId(), testEquipment2.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5)
        );

        String json = """
            {
              "id": %d,
              "equipmentIds": [%d]
            }
            """.formatted(created.getId(), testEquipment1.getId());

        mockMvc.perform(delete("/api/v1/reservation/equipment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), userToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Test Helmet"))))
            .andExpect(content().string(containsString("Test Ski")));
    }

    @Test
    public void addEquipmentToReservation_withValidData_returns200AndAddsItem() throws Exception {
        ReservationDetailDto created = createTestReservation(
            List.of(testEquipment1.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5)
        );

        String json = """
            {
              "id": %d,
              "equipmentIds": [%d]
            }
            """.formatted(created.getId(), testEquipment2.getId());

        mockMvc.perform(post("/api/v1/reservation/equipment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), userToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Test Helmet")))
            .andExpect(content().string(containsString("Test Ski")));
    }

    @Test
    public void createReservation_withUnknownCustomerProfileId_returns400() throws Exception {
        String json = """
            {
              "customerProfileId": 99999,
              "equipmentIds": [%d],
              "startDate": "%s",
              "endDate": "%s",
              "pickUpTime": "10:00:00",
              "reservationStatus": "CREATED"
            }
            """.formatted(
            testEquipment1.getId(),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5)
        );

        mockMvc.perform(post("/api/v1/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), userToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void updateReservation_withUnknownReservationId_returns400() throws Exception {
        long unknownId = 99999L;

        String patchJson = """
            {
              "id": %d,
              "customerProfileId": %d,
              "equipmentIds": [%d],
              "startDate": "%s",
              "endDate": "%s",
              "pickUpTime": "10:00:00",
              "reservationStatus": "PICKED_UP"
            }
            """.formatted(
            unknownId,
            testProfile.getId(),
            testEquipment1.getId(),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5)
        );

        mockMvc.perform(patch("/api/v1/reservation/{id}", unknownId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson)
                .header(securityProperties.getAuthHeader(), userToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void removeEquipmentFromReservation_withUnknownReservationId_returns404() throws Exception {
        String json = """
            {
              "id": 99999,
              "equipmentIds": [%d]
            }
            """.formatted(testEquipment1.getId());

        mockMvc.perform(delete("/api/v1/reservation/equipment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), userToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void searchReservations_withMatchingParams_returns200AndFilteredList() throws Exception {
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(13);

        createTestReservation(
            List.of(testEquipment1.getId()),
            startDate,
            endDate
        );

        mockMvc.perform(get("/api/v1/reservation")
                .param("customerProfileId", testProfile.getId().toString())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("reservationStatus", "CREATED")
                .header(securityProperties.getAuthHeader(), userToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Max Mustermann")))
            .andExpect(content().string(containsString("Test Helmet")))
            .andExpect(content().string(containsString("CREATED")));
    }

    @Test
    public void searchReservations_withNoMatchingParams_returns200AndEmptyList() throws Exception {
        createTestReservation(
            List.of(testEquipment1.getId()),
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(5)
        );

        mockMvc.perform(get("/api/v1/reservation")
                .param("customerProfileId", "99999")
                .param("startDate", "2099-01-01")
                .param("endDate", "2099-01-05")
                .param("reservationStatus", "CREATED")
                .header(securityProperties.getAuthHeader(), userToken())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    private ReservationDetailDto createTestReservation(
        List<Long> equipmentIds,
        LocalDate startDate,
        LocalDate endDate
    ) {
        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setCustomerProfileId(testProfile.getId());
        createDto.setEquipmentIds(equipmentIds);
        createDto.setStartDate(startDate);
        createDto.setEndDate(endDate);
        createDto.setPickUpTime(LocalTime.of(10, 0));
        createDto.setReservationStatus(ReservationStatus.CREATED);

        return reservationService.createReservation(createDto);
    }
}