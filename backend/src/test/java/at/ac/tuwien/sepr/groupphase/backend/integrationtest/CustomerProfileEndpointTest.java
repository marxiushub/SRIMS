package at.ac.tuwien.sepr.groupphase.backend.integrationtest;


import at.ac.tuwien.sepr.groupphase.backend.basetest.IntegrationTestBase;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.entity.Permission;
import at.ac.tuwien.sepr.groupphase.backend.entity.Role;
import at.ac.tuwien.sepr.groupphase.backend.entity.enums.SkillLevel;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.CustomerProfile;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"test"})
@AutoConfigureMockMvc
@SpringBootTest
public class CustomerProfileEndpointTest extends IntegrationTestBase implements TestData {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenizer jwtTokenizer;
    @Autowired
    private SecurityProperties securityProperties;

    //Creates Test Customer
    private Customer createTestCustomer(String suffix) {
        Customer customer = new Customer(
            "profile_user_" + suffix,
            "hashedPassword",
            "profile.user." + suffix + "@example.com",
            Set.of(),
            Set.of(),
            "Profile",
            "Tester",
            LocalDate.of(1999, 1, 1)
        );

        return customerRepository.save(customer);
    }

    //Creates Test Customer Profile
    private CustomerProfile createTestProfile(Customer customer, String profileName, SkillLevel skillLevel) {
        CustomerProfile profile = new CustomerProfile(
            profileName,
            175,
            70,
            42,
            skillLevel,
            customer
        );

        return customerProfileRepository.save(profile);
    }

    //Authentication helper
    private String userToken(Customer customer) {
        return jwtTokenizer.getAuthToken(
            customer.getEmail(),
            customer.getId(),
            USER_PERMISSIONS
        );
    }

    //Creates Test-Staff-User
    private Staff createTestStaff(String suffix) {
        Staff staff = new Staff(
            "staff_user_" + suffix,
            "hashedPassword",
            "staff.user." + suffix + "@example.com",
            Set.<Role>of(),
            Set.<Permission>of()
        );

        return staffRepository.save(staff);
    }

    //Creates a Token for a Staff-User
    private String staffToken(Staff staff) {
        return jwtTokenizer.getAuthToken(
            staff.getEmail(),
            staff.getId(),
            ADMIN_PERMISSIONS
        );
    }

    @Test
    public void createCustomerProfile_withValidDto_returns201AndSavedProfile() throws Exception {
        Customer savedCustomer = createTestCustomer("create_valid");

        String json = """
        {
          "profileName": "Endpoint Test Profile",
          "height": 175,
          "weight": 70,
          "shoeSize": 42,
          "skillLevel": "BEGINNER"
        }
        """;

        mockMvc.perform(post("/api/v1/customer/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), userToken(savedCustomer))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.profileName").value("Endpoint Test Profile"))
            .andExpect(jsonPath("$.customerId").value(savedCustomer.getId()));
    }

    @Test
    public void createCustomerProfile_withUnknownCustomerInToken_returns404() throws Exception {
        Customer authCustomer = createTestCustomer("create_unknown_auth");

        String json = """
        {
          "profileName": "Unknown Customer Profile",
          "height": 175,
          "weight": 70,
          "shoeSize": 42,
          "skillLevel": "BEGINNER"
        }
        """;

        String invalidToken = jwtTokenizer.getAuthToken(
            authCustomer.getEmail(),
            99999L,
            List.of("CUSTOMERPROFILE_CREATE")
        );

        mockMvc.perform(post("/api/v1/customer/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), invalidToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void createCustomerProfile_withInvalidDto_returns400() throws Exception {
        Customer customer = createTestCustomer("create_invalid");

        String json = """
            {
              "profileName": "",
              "height": 175,
              "weight": 70,
              "shoeSize": 42,
              "skillLevel": "BEGINNER"
            }
            """.formatted(customer.getId());

        mockMvc.perform(post("/api/v1/customer/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), userToken(customer))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void getCustomerProfiles_withExistingCustomer_returns200AndProfiles() throws Exception {
        Customer customer = createTestCustomer("get_profiles");

        createTestProfile(customer, "Endpoint First Profile", SkillLevel.BEGINNER);
        createTestProfile(customer, "Endpoint Second Profile", SkillLevel.ADVANCED);

        mockMvc.perform(get("/api/v1/customer/profiles")
                .header(securityProperties.getAuthHeader(), userToken(customer))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].profileName").value(hasItem("Endpoint First Profile")))
            .andExpect(jsonPath("$[*].profileName").value(hasItem("Endpoint Second Profile")))
            .andExpect(jsonPath("$[*].customerId").value(hasItem(customer.getId().intValue())));
    }

    @Test
    public void getCustomerProfiles_withUnknownCustomer_returns404() throws Exception {

        String token = jwtTokenizer.getAuthToken(
            "unknown@test.at",
            99999L,
            List.of("CUSTOMERPROFILE_READ")
        );

        mockMvc.perform(get("/api/v1/customer/profiles")
                .header(securityProperties.getAuthHeader(), token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void getCustomerProfiles_asStaff_withExistingCustomer_returns200AndProfiles() throws Exception {
        Customer customer = createTestCustomer("staff_get_profiles");

        createTestProfile(customer, "Staff First Profile", SkillLevel.BEGINNER);
        createTestProfile(customer, "Staff Second Profile", SkillLevel.ADVANCED);

        Staff staff = createTestStaff("reader");

        mockMvc.perform(get("/api/v1/customer/{customerId}/profiles", customer.getId())
                .header(securityProperties.getAuthHeader(), staffToken(staff))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].profileName").value(hasItem("Staff First Profile")))
            .andExpect(jsonPath("$[*].profileName").value(hasItem("Staff Second Profile")))
            .andExpect(jsonPath("$[*].customerId").value(hasItem(customer.getId().intValue())));
    }

    @Test
    public void getCustomerProfiles_asStaff_withUnknownCustomer_returns404() throws Exception {

        Staff staff = createTestStaff("reader");

        mockMvc.perform(get("/api/v1/customer/{customerId}/profiles", 99999L)
                .header(securityProperties.getAuthHeader(), staffToken(staff))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void getCustomerProfiles_asCustomer_onStaffEndpoint_returns403() throws Exception {
        Customer customer = createTestCustomer("no_staff_access");

        String token = userToken(customer);

        mockMvc.perform(get("/api/v1/customer/{customerId}/profiles", 1L)
                .header(securityProperties.getAuthHeader(), token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    public void getCustomerProfiles_asStaffWithoutPermission_returns403() throws Exception {
        Customer customer = createTestCustomer("restricted");

        Staff staff = createTestStaff("no_permission");

        String token = jwtTokenizer.getAuthToken(
            staff.getEmail(),
            staff.getId(),
            List.of("STAFF") // missing CUSTOMERPROFILE_READ
        );

        mockMvc.perform(get("/api/v1/customer/{customerId}/profiles", customer.getId())
                .header(securityProperties.getAuthHeader(), token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    public void deleteCustomerProfile_withExistingProfile_returns204AndDeletesProfile() throws Exception {
        Customer customer = createTestCustomer("delete_valid");
        CustomerProfile profile = createTestProfile(customer, "Endpoint Profile To Delete", SkillLevel.BEGINNER);

        mockMvc.perform(delete("/api/v1/customer/profiles/{profileId}", profile.getId())
                .header(securityProperties.getAuthHeader(), userToken(customer)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/customer/profiles/{profileId}", profile.getId())
                .header(securityProperties.getAuthHeader(), userToken(customer))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void deleteCustomerProfile_withUnknownProfile_returns404() throws Exception {
        Customer authCustomer = createTestCustomer("delete_unknown_auth");

        mockMvc.perform(delete("/api/v1/customer/profiles/{profileId}", 99999L)
                .header(securityProperties.getAuthHeader(), userToken(authCustomer)))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateCustomerProfile_withValidDto_returns200AndUpdatedProfile() throws Exception {
        Customer customer = createTestCustomer("update_valid");
        CustomerProfile profile = createTestProfile(customer, "Endpoint Old Profile", SkillLevel.BEGINNER);

        String json = """
            {
              "profileName": "Endpoint Updated Profile",
              "height": 181,
              "skillLevel": "INTERMEDIATE"
            }
            """;

        mockMvc.perform(patch("/api/v1/customer/profiles/{profileId}", profile.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), userToken(customer))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profileName").value("Endpoint Updated Profile"))
            .andExpect(jsonPath("$.height").value(181.0))
            .andExpect(jsonPath("$.weight").value(70.0))
            .andExpect(jsonPath("$.shoeSize").value(42.0))
            .andExpect(jsonPath("$.skillLevel").value("INTERMEDIATE"));
    }

    @Test
    public void updateCustomerProfile_withUnknownProfile_returns404() throws Exception {
        Customer authCustomer = createTestCustomer("update_unknown_auth");

        String json = """
            {
              "profileName": "Unknown Profile Update"
            }
            """;

        mockMvc.perform(patch("/api/v1/customer/profiles/{profileId}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), userToken(authCustomer))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void updateCustomerProfile_withEmptyDto_returns400() throws Exception {
        Customer customer = createTestCustomer("update_empty");
        CustomerProfile profile = createTestProfile(customer, "Endpoint Empty Update Profile", SkillLevel.BEGINNER);

        String json = """
            {
            }
            """;

        mockMvc.perform(patch("/api/v1/customer/profiles/{profileId}", profile.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), userToken(customer))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
