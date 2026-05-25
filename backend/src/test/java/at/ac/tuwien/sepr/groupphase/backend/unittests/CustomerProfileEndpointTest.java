package at.ac.tuwien.sepr.groupphase.backend.unittests;


import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerProfileRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import jakarta.transaction.Transactional;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles({"test", "generateData"})
@AutoConfigureMockMvc
@SpringBootTest
public class CustomerProfileEndpointTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Test
    @Transactional
    @Rollback
    public void createCustomerProfile_withValidDto_returns201AndSavedProfile() {
        Customer customer = new Customer(
            "endpoint_profile_user",
            "hashedPassword",
            "endpoint.profile@example.com",
            "Endpoint",
            "Tester",
            LocalDate.of(1999, 1, 1)
        );
        Customer savedCustomer = customerRepository.save(customer);

        String json = """
            {
              "customerId": %d,
              "profileName": "Endpoint Test Profile",
              "height": 175,
              "weight": 70,
              "shoeSize": 42,
              "skillLevel": "BEGINNER"
            }
            """.formatted(savedCustomer.getId());

        try {
            MvcResult result = mockMvc.perform(post("/api/v1/customer/profiles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check if customer profile was successfully created via endpoint",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(201),
                () -> assertThat(responseBody).contains("Endpoint Test Profile"),
                () -> assertThat(responseBody).contains("\"customerId\":" + savedCustomer.getId()),
                () -> assertThat(customerProfileRepository.findAll().stream()
                    .anyMatch(profile -> "Endpoint Test Profile".equals(profile.getProfileName())))
                    .isTrue()
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Transactional
    @Rollback
    public void createCustomerProfile_withUnknownCustomerId_returns404() {
        String json = """
            {
              "customerId": 99999,
              "profileName": "Unknown Customer Profile",
              "height": 175,
              "weight": 70,
              "shoeSize": 42,
              "skillLevel": "BEGINNER"
            }
            """;

        try {
            MvcResult result = mockMvc.perform(post("/api/v1/customer/profiles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            assertAll(
                "Check if creating a profile for an unknown customer returns 404",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(404)
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Transactional
    @Rollback
    public void createCustomerProfile_withInvalidDto_returns400() {
        Customer customer = new Customer(
            "invalid_profile_user",
            "hashedPassword",
            "invalid.profile@example.com",
            "Invalid",
            "Tester",
            LocalDate.of(1999, 1, 1)
        );
        customer = customerRepository.save(customer);

        String json = """
            {
              "customerId": %d,
              "profileName": "",
              "height": 175,
              "weight": 70,
              "shoeSize": 42,
              "skillLevel": "BEGINNER"
            }
            """.formatted(customer.getId());

        try {
            MvcResult result = mockMvc.perform(post("/api/v1/customer/profiles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            assertAll(
                "Check if invalid customer profile data returns 400",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(400)
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage());
        }
    }
}
