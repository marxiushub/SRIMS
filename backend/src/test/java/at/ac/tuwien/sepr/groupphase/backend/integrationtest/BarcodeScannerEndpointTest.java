package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.IntegrationTestBase;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.datagenerator.DataInitializer;
import at.ac.tuwien.sepr.groupphase.backend.entity.Reservation;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.ReservationStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Ski;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import at.ac.tuwien.sepr.groupphase.backend.repository.ReservationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.StaffRepository;
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
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles({"test", "datagenerator", "generateData"})
@SpringBootTest
@AutoConfigureMockMvc
public class BarcodeScannerEndpointTest extends IntegrationTestBase implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private DataInitializer initializer;

    @Autowired
    private ReservationRepository reservationRepository;


    private String staffToken;
    private String customerToken;
    private CustomerProfile testProfile;
    private Helmet testHelmet;
    private Ski testSki;
    private Reservation testReservation;

    @BeforeEach
    public void setup() {
        initializer.initializeData();

        Staff staffUser = staffRepository.findByEmail("admin@email.com").orElseThrow();

        staffToken = jwtTokenizer.getAuthToken(
            staffUser.getEmail(),
            staffUser.getId(),
            ADMIN_PERMISSIONS
        );


        Customer customer = customerRepository.findByEmail("benjamin.marius.widmer@gmail.com").orElseThrow();

        customerToken = jwtTokenizer.getAuthToken(
            customer.getEmail(),
            customer.getId(),
            USER_PERMISSIONS
        );

        testProfile = new CustomerProfile(
            "Max Mustermann",
            180,
            75,
            42,
            SkillLevel.ADVANCED,
            customer
        );
        testProfile = customerProfileRepository.save(testProfile);

        testHelmet = new Helmet(
            "Test Helmet",
            15.0,
            58.0,
            RentalStatus.FREE,
            SkillLevel.BEGINNER
        );
        testHelmet = equipmentRepository.save(testHelmet);

        testSki = new Ski(
            "Test Ski",
            50.0,
            170.0,
            RentalStatus.FREE,
            SkillLevel.ADVANCED
        );
        testSki = equipmentRepository.save(testSki);

        testReservation = new Reservation(
            testProfile,
            LocalTime.of(10, 0),
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            ReservationStatus.PICKED_UP
        );

        testReservation.setTotalPrice(100.0);
        testReservation.setOverdueReminderSent(false);
        testReservation.setPickUpReminderSent(false);

        testReservation = reservationRepository.save(testReservation);
    }

    @Test
    public void patch_existingReservation_returnsReservationDetail() throws Exception {

        Long reservationId = testReservation.getId();

        String json = """
{
  "id": %d,
  "barcode": "TEST-BARCODE-123",
  "reservationStatus": "PICKED_UP",
  "equipmentIds": [%d],
  "action": "CHECK_OUT"
}
""".formatted(reservationId, testSki.getId());

        mockMvc.perform(patch("/api/v1/scanner/%d".formatted(reservationId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), staffToken))
            .andExpect(status().isOk());
    }

    @Test
    public void post_withoutExistingReservation_returnsReservationDetail() throws Exception {

        String json = """
    {
      "customerProfileId": %d,
      "equipmentIds": [%d, %d],
      "pickUpTime": "10:00:00",
      "startDate": "%s",
      "endDate": "%s",
      "reservationStatus": "PICKED_UP",
      "mode": "RENTAL"
    }
    """.formatted(
            testProfile.getId(),
            testHelmet.getId(),
            testSki.getId(),
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2)
        );

        mockMvc.perform(post("/api/v1/scanner")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), staffToken))
            .andExpect(status().isOk());
    }

    @Test
    public void patch_withoutAuthority_returns403() throws Exception {

        String json = """
        {
          "barcode": "TEST-BARCODE-123",
          "action": "CHECK_OUT"
        }
        """;

        mockMvc.perform(patch("/api/v1/scanner/%d".formatted(testProfile.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isForbidden());
    }

    @Test
    public void post_invalidBody_returns400() throws Exception {

        String json = """
        {
          "customerProfileId": null,
          "equipmentIds": [],
          "pickUpTime": null,
          "startDate": null,
          "endDate": null,
          "reservationStatus": "CREATED",
          "mode": "RENTAL"
        }
        """;

        mockMvc.perform(post("/api/v1/scanner")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), staffToken))
            .andExpect(status().isBadRequest());
    }
}
