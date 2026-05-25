package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.StaffRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


@ActiveProfiles({"test", "generateData"})
@AutoConfigureMockMvc
@SpringBootTest
public class UserEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StaffRepository staffRepository;


    @Test
    @Transactional
    @Rollback
    public void createCustomer_withValidDto_returns200AndSavedCustomer() {

        String json = """
        {
          "type": "CUSTOMER",
          "userName": "max_customer",
          "password": "Password123!",
          "email": "max.customer@test.at",
          "firstName": "Max",
          "lastName": "Mustermann",
          "dateOfBirth": "1998-05-10"
        }
        """;

        try {
            MvcResult result = mockMvc.perform(post("/api/v1/customer/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check if customer was successfully created",

                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).contains("max.customer@test.at"),
                () -> assertThat(responseBody).contains("max_customer")
            );

        } catch (Exception e) {
            fail("Test failed because of unexpected exception" + e.getMessage(), e);
        }
    }


    @Test
    @Transactional
    @Rollback
    public void createStaff_withValidDto_returns200AndSavedStaff() {

        String json = """
        {
          "type": "STAFF",
          "userName": "staff_user",
          "password": "Password123!",
          "email": "staff@test.at"
        }
        """;

        try {
            MvcResult result = mockMvc.perform(post("/api/v1/staff/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check if staff was successfully created",

                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).contains("staff@test.at"),
                () -> assertThat(responseBody).contains("staff_user")
            );

        } catch (Exception e) {
            fail("Test failed because of unexpected exception" + e.getMessage(), e);
        }
    }


    @Test
    @Transactional
    @Rollback
    public void updateCustomer_withGeneratedCustomer_returns200AndUpdatedCustomer() {

        try {
            String createJson = """
            {
              "type": "CUSTOMER",
              "userName": "customer_to_update",
              "password": "Password123!",
              "email": "customer.to.update@test.at",
              "firstName": "OldFirst",
              "lastName": "OldLast",
              "dateOfBirth": "1990-01-01"
            }
            """;

            MvcResult createResult = mockMvc.perform(
                    post("/api/v1/customer/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andReturn();

            assertThat(createResult.getResponse().getStatus()).isEqualTo(200);

            Long customerId = customerRepository.findByEmail("customer.to.update@test.at")
                .orElseThrow()
                .getId();


            String updateJson = """
        {
          "type": "CUSTOMER",
          "userName": "updated_retro_gamer",
          "email": "updated.marcel@example.com",
          "firstName": "UpdatedMarcel",
          "lastName": "UpdatedNeumann",
          "dateOfBirth": "1993-03-03"
        }
        """;

            MvcResult result = mockMvc.perform(
                    put("/api/v1/customer/update/" + customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check if generated customer was successfully updated",

                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),

                // alle geänderten Felder prüfen
                () -> assertThat(responseBody).contains("updated_retro_gamer"),
                () -> assertThat(responseBody).contains("updated.marcel@example.com"),
                () -> assertThat(responseBody).contains("UpdatedMarcel"),
                () -> assertThat(responseBody).contains("UpdatedNeumann"),
                () -> assertThat(responseBody).contains("1993-03-03")
            );

        } catch (Exception e) {
            fail("Test failed because of unexpected exception" + e.getMessage(), e);
        }
    }


    @Test
    @Transactional
    @Rollback
    public void updateStaff_withGeneratedStaff_returns200AndUpdatedStaff() {

        try {
            String createJson = """
            {
              "type": "STAFF",
              "userName": "staff_to_update",
              "password": "Password123!",
              "email": "staff.to.update@test.at"
            }
            """;

            MvcResult createResult = mockMvc.perform(
                    post("/api/v1/staff/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andReturn();

            assertThat(createResult.getResponse().getStatus()).isEqualTo(200);

            Long staffId = staffRepository.findByEmail("staff.to.update@test.at")
                .orElseThrow()
                .getId();

            String updateJson = """
        {
          "type": "STAFF",
          "userName": "updated_admin",
          "email": "updated.admin@system.com"
        }
        """;

            MvcResult result = mockMvc.perform(
                    put("/api/v1/staff/update/" + staffId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check if generated staff was successfully updated",

                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),

                // alle geänderten Felder prüfen
                () -> assertThat(responseBody).contains("updated_admin"),
                () -> assertThat(responseBody).contains("updated.admin@system.com")
            );

        } catch (Exception e) {
            fail("Test failed because of unexpected exception" + e.getMessage(), e);
        }
    }


    @Test
    @Transactional
    @Rollback
    public void deleteCustomer_withExistingId_returns200AndDeletesCustomer() {

        try {
            Long customerId = customerRepository.findByEmail("marcel.neumann@example.com")
                .orElseThrow()
                .getId();

            MvcResult result = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/customer/delete/" + customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn();

            assertAll(
                "Check if customer was successfully deleted",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(userRepository.findById(customerId)).isEmpty(),
                () -> assertThat(customerRepository.findById(customerId)).isEmpty()
            );

        } catch (Exception e) {
            fail("Test failed because of unexpected exception");
        }
    }


    @Test
    @Transactional
    @Rollback
    public void deleteStaff_withExistingId_returns200AndDeletesStaff() {

        try {
            Long staffId = staffRepository.findByEmail("admin.core@system.com")
                .orElseThrow()
                .getId();

            MvcResult result = mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/staff/delete/" + staffId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn();

            assertAll(
                "Check if staff was successfully deleted",

                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(userRepository.findById(staffId)).isEmpty(),
                () -> assertThat(staffRepository.findById(staffId)).isEmpty()
            );

        } catch (Exception e) {
            fail("Test failed because of unexpected exception" + e.getMessage(), e);
        }
    }
}
