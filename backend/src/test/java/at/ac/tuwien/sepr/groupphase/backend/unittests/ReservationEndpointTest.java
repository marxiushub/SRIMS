package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationCreationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.reservationdto.ReservationDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.RentalStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Equipment;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Helmet;
import at.ac.tuwien.sepr.groupphase.backend.entity.equipment.Ski;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.repository.equipment.EquipmentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ReservationService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ActiveProfiles({"test", "datagenerator"})
@AutoConfigureMockMvc
@SpringBootTest
public class ReservationEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    private CustomerProfile testProfile;
    private Equipment testEquipment1;
    private Equipment testEquipment2;

    @BeforeEach
    public void setup() {
        Customer customer = new Customer("Test", "User", "test@user.com", "123", "456", LocalDate.of(1990, 1, 1));
        customer = customerRepository.save(customer);

        testProfile = new CustomerProfile("Max Mustermann", 180, 75, 42, SkillLevel.ADVANCED, customer);
        testProfile = customerProfileRepository.save(testProfile);

        testEquipment1 = new Helmet("Test Helmet", 15.0, 58.0, RentalStatus.FREE, SkillLevel.BEGINNER);
        testEquipment1 = equipmentRepository.save(testEquipment1);

        testEquipment2 = new Ski("Test Ski", 50.0, 170.0, RentalStatus.FREE, SkillLevel.ADVANCED);
        testEquipment2 = equipmentRepository.save(testEquipment2);
    }

    @Test
    @Transactional
    @Rollback
    public void createReservation_withValidDto_returns200AndSavedData() {

        String json = """
            {
              "customerProfileId": %d,
              "equipmentIds": [%d],
              "pickUpDate": "%s",
              "pickUpTime": "10:00:00",
              "rentDurationDays": 3
            }
            """.formatted(
            testProfile.getId(),
            testEquipment1.getId(),
            LocalDate.now().plusDays(2).toString()
        );

        try {
            MvcResult result = mockMvc.perform(post("/api/v1/reservation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check if reservation was successfully created via Endpoint",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).contains("Max Mustermann"),
                () -> assertThat(responseBody).contains("Test Helmet")
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Transactional
    @Rollback
    public void updateReservation_withValidDto_returns200AndUpdatedFields() {
        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setCustomerProfileId(testProfile.getId());
        createDto.setEquipmentIds(List.of(testEquipment1.getId()));
        createDto.setPickUpDate(LocalDate.now().plusDays(2));
        createDto.setPickUpTime(LocalTime.of(10, 0));
        createDto.setRentDurationDays(3);

        ReservationDetailDto created = reservationService.createReservation(createDto);

        String patchJson = """
            {
              "id": %d,
              "customerProfileId": %d,
              "equipmentIds": [%d],
              "pickUpDate": "%s",
              "pickUpTime": "10:00:00",
              "rentDurationDays": 5
            }
            """.formatted(
            created.getId(),
            testProfile.getId(),
            testEquipment1.getId(),
            LocalDate.now().plusDays(10).toString()
        );

        try {
            MvcResult result = mockMvc.perform(patch("/api/v1/reservation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check HTTP-Status and whether the duration was updated",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).contains("\"rentDurationDays\":5")
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Transactional
    @Rollback
    public void removeEquipmentFromReservation_withValidData_returns200AndRemovesItem() {
        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setCustomerProfileId(testProfile.getId());
        createDto.setEquipmentIds(List.of(testEquipment1.getId(), testEquipment2.getId()));
        createDto.setPickUpDate(LocalDate.now().plusDays(2));
        createDto.setPickUpTime(LocalTime.of(10, 0));
        createDto.setRentDurationDays(3);

        ReservationDetailDto created = reservationService.createReservation(createDto);

        String json = """
            {
              "id": %d,
              "equipmentIds": [%d]
            }
            """.formatted(created.getId(), testEquipment1.getId());

        try {
            MvcResult result = mockMvc.perform(delete("/api/v1/reservation/equipment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check if equipment was removed correctly",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).doesNotContain("Test Helmet"),
                () -> assertThat(responseBody).contains("Test Ski")
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Transactional
    @Rollback
    public void addEquipmentToReservation_withValidData_returns200AndAddsItem() {
        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setCustomerProfileId(testProfile.getId());
        createDto.setEquipmentIds(List.of(testEquipment1.getId()));
        createDto.setPickUpDate(LocalDate.now().plusDays(2));
        createDto.setPickUpTime(LocalTime.of(10, 0));
        createDto.setRentDurationDays(3);

        ReservationDetailDto created = reservationService.createReservation(createDto);

        String json = """
            {
              "id": %d,
              "equipmentIds": [%d]
            }
            """.formatted(created.getId(), testEquipment2.getId());

        try {
            MvcResult result = mockMvc.perform(post("/api/v1/reservation/equipment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check if equipment was added correctly",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).contains("Test Helmet"),
                () -> assertThat(responseBody).contains("Test Ski")
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Transactional
    @Rollback
    public void createReservation_withUnknownCustomerProfileId_returns404() {
        String json = """
            {
              "customerProfileId": 99999,
              "equipmentIds": [%d],
              "pickUpDate": "%s",
              "pickUpTime": "10:00:00",
              "rentDurationDays": 3
            }
            """.formatted(
            testEquipment1.getId(),
            LocalDate.now().plusDays(2).toString()
        );

        try {
            MvcResult result = mockMvc.perform(post("/api/v1/reservation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            assertAll(
                "Check if creating a reservation for an unknown user returns 404 Not Found",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(404)
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Transactional
    @Rollback
    public void updateReservation_withUnknownReservationId_returns400() {
        String patchJson = """
            {
              "id": 99999,
              "customerProfileId": %d,
              "equipmentIds": [%d],
              "pickUpDate": "%s",
              "pickUpTime": "10:00:00",
              "rentDurationDays": 5
            }
            """.formatted(
            testProfile.getId(),
            testEquipment1.getId(),
            LocalDate.now().plusDays(2).toString()
        );

        try {
            MvcResult result = mockMvc.perform(patch("/api/v1/reservation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(patchJson))
                .andReturn();

            assertAll(
                "Check if updating an unknown reservation returns 400 Bad Request",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(400)
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Transactional
    @Rollback
    public void removeEquipmentFromReservation_withUnknownReservationId_returns404() {
        String json = """
            {
              "id": 99999,
              "equipmentIds": [%d]
            }
            """.formatted(testEquipment1.getId());

        try {
            MvcResult result = mockMvc.perform(delete("/api/v1/reservation/equipment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            assertAll(
                "Check if deleting equipment from an unknown reservation returns 404",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(404)
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Transactional
    @Rollback
    public void searchReservations_withMatchingParams_returns200AndFilteredList() {
        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setCustomerProfileId(testProfile.getId());
        createDto.setEquipmentIds(List.of(testEquipment1.getId()));
        createDto.setPickUpDate(LocalDate.now().plusDays(10));
        createDto.setPickUpTime(LocalTime.of(10, 0));
        createDto.setRentDurationDays(3);

        reservationService.createReservation(createDto);

        try {
            MvcResult result = mockMvc.perform(get("/api/v1/reservation")
                    .param("customerProfileId", testProfile.getId().toString())
                    .param("pickUpDate", LocalDate.now().plusDays(10).toString())
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check if the search endpoint correctly filters and returns 200",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).contains("Max Mustermann"),
                () -> assertThat(responseBody).contains("Test Helmet")
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Transactional
    @Rollback
    public void searchReservations_withNoMatchingParams_returns200AndEmptyList() {
        ReservationCreationDto createDto = new ReservationCreationDto();
        createDto.setCustomerProfileId(testProfile.getId());
        createDto.setEquipmentIds(List.of(testEquipment1.getId()));
        createDto.setPickUpDate(LocalDate.now().plusDays(2));
        createDto.setPickUpTime(LocalTime.of(10, 0));
        createDto.setRentDurationDays(3);

        reservationService.createReservation(createDto);

        try {
            MvcResult result = mockMvc.perform(get("/api/v1/reservation")
                    .param("customerProfileId", "99999")
                    .param("pickUpDate", "2099-01-01")
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check if a search without matches returns an empty JSON array",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).isEqualTo("[]")
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage());
        }
    }
}