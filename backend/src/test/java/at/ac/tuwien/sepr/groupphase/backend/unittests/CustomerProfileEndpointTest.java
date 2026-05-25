package at.ac.tuwien.sepr.groupphase.backend.unittests;


import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

//AI-assisted
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

    @Test
    @Transactional
    @Rollback
    public void getCustomerProfiles_withExistingCustomer_returns200AndProfiles() {
        Customer customer = new Customer(
            "endpoint_list_profile_user",
            "hashedPassword",
            "endpoint.list.profile@example.com",
            "EndpointList",
            "Tester",
            LocalDate.of(1999, 1, 1)
        );

        Customer savedCustomer = customerRepository.save(customer);

        CustomerProfile firstProfile = new CustomerProfile(
            "Endpoint First Profile",
            175,
            70,
            42,
            SkillLevel.BEGINNER,
            savedCustomer
        );

        CustomerProfile secondProfile = new CustomerProfile(
            "Endpoint Second Profile",
            180,
            80,
            44,
            SkillLevel.ADVANCED,
            savedCustomer
        );

        customerProfileRepository.save(firstProfile);
        customerProfileRepository.save(secondProfile);

        try {
            MvcResult result = mockMvc.perform(get("/api/v1/customer/" + savedCustomer.getId() + "/profiles")
                    .accept(MediaType.APPLICATION_JSON))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            assertAll(
                "Check if customer profiles were successfully returned via endpoint",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).contains("Endpoint First Profile"),
                () -> assertThat(responseBody).contains("Endpoint Second Profile"),
                () -> assertThat(responseBody).contains("\"customerId\":" + savedCustomer.getId())
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage(), e);
        }
    }

    @Test
    @Transactional
    @Rollback
    public void getCustomerProfiles_withUnknownCustomer_returns404() {
        try {
            MvcResult result = mockMvc.perform(get("/api/v1/customer/99999/profiles")
                    .accept(MediaType.APPLICATION_JSON))
                .andReturn();

            assertAll(
                "Check if getting profiles for an unknown customer returns 404",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(404)
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage(), e);
        }
    }

    @Test
    @Transactional
    @Rollback
    public void deleteCustomerProfile_withExistingProfile_returns204AndDeletesProfile() {
        Customer customer = new Customer(
            "endpoint_delete_profile_user",
            "hashedPassword",
            "endpoint.delete.profile@example.com",
            "EndpointDelete",
            "Tester",
            LocalDate.of(1999, 1, 1)
        );

        Customer savedCustomer = customerRepository.save(customer);

        CustomerProfile profile = new CustomerProfile(
            "Endpoint Profile To Delete",
            175,
            70,
            42,
            SkillLevel.BEGINNER,
            savedCustomer
        );

        CustomerProfile savedProfile = customerProfileRepository.save(profile);

        try {
            MvcResult result = mockMvc.perform(delete("/api/v1/customer/profiles/" + savedProfile.getId()))
                .andReturn();

            assertAll(
                "Check if customer profile was deleted via endpoint",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(204),
                () -> assertThat(customerProfileRepository.existsById(savedProfile.getId())).isFalse()
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage(), e);
        }
    }

    @Test
    @Transactional
    @Rollback
    public void deleteCustomerProfile_withUnknownProfile_returns404() {
        try {
            MvcResult result = mockMvc.perform(delete("/api/v1/customer/profiles/99999"))
                .andReturn();

            assertAll(
                "Check if deleting an unknown customer profile returns 404",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(404)
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage(), e);
        }
    }

    @Test
    @Transactional
    @Rollback
    public void updateCustomerProfile_withValidDto_returns200AndUpdatedProfile() {
        Customer customer = new Customer(
            "endpoint_update_profile_user",
            "hashedPassword",
            "endpoint.update.profile@example.com",
            "EndpointUpdate",
            "Tester",
            LocalDate.of(1999, 1, 1)
        );

        Customer savedCustomer = customerRepository.save(customer);

        CustomerProfile profile = new CustomerProfile(
            "Endpoint Old Profile",
            175,
            70,
            42,
            SkillLevel.BEGINNER,
            savedCustomer
        );

        CustomerProfile savedProfile = customerProfileRepository.save(profile);

        String json = """
        {
          "profileName": "Endpoint Updated Profile",
          "height": 181,
          "skillLevel": "INTERMEDIATE"
        }
        """;

        try {
            MvcResult result = mockMvc.perform(patch("/api/v1/customer/profiles/" + savedProfile.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();

            CustomerProfile updatedProfile = customerProfileRepository.findById(savedProfile.getId()).orElseThrow();

            assertAll(
                "Check if customer profile was successfully updated via endpoint",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(200),
                () -> assertThat(responseBody).contains("Endpoint Updated Profile"),
                () -> assertThat(responseBody).contains("\"height\":181.0"),
                () -> assertThat(responseBody).contains("INTERMEDIATE"),

                () -> assertThat(updatedProfile.getProfileName()).isEqualTo("Endpoint Updated Profile"),
                () -> assertThat(updatedProfile.getHeight()).isEqualTo(181.0),
                () -> assertThat(updatedProfile.getWeight()).isEqualTo(70),
                () -> assertThat(updatedProfile.getShoeSize()).isEqualTo(42),
                () -> assertThat(updatedProfile.getSkillLevel()).isEqualTo(SkillLevel.INTERMEDIATE)
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage(), e);
        }
    }

    @Test
    @Transactional
    @Rollback
    public void updateCustomerProfile_withUnknownProfile_returns404() {
        String json = """
        {
          "profileName": "Unknown Profile Update"
        }
        """;

        try {
            MvcResult result = mockMvc.perform(patch("/api/v1/customer/profiles/99999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            assertAll(
                "Check if updating an unknown customer profile returns 404",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(404)
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage(), e);
        }
    }

    @Test
    @Transactional
    @Rollback
    public void updateCustomerProfile_withEmptyDto_returns400() {
        String json = """
        {
        }
        """;

        try {
            MvcResult result = mockMvc.perform(patch("/api/v1/customer/profiles/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andReturn();

            assertAll(
                "Check if empty update data returns 400",
                () -> assertThat(result.getResponse().getStatus()).isEqualTo(400)
            );
        } catch (Exception e) {
            fail("Test failed because of unexpected exception: " + e.getMessage(), e);
        }
    }
}
