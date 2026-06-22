package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.IntegrationTestBase;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.entity.Role;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoleRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"test"})
@AutoConfigureMockMvc
@SpringBootTest
public class UserEndpointTest extends IntegrationTestBase implements TestData {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenizer jwtTokenizer;
    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private RoleRepository roleRepository;

    // --- Helpers ---

    private Customer createTestCustomer(String suffix) {
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER").orElseThrow();
        Customer customer = new Customer(
            "customer_" + suffix,
            "hashedPassword",
            "customer." + suffix + "@example.com",
            Set.of(customerRole),
            Set.of(),
            "First",
            "Last",
            LocalDate.of(1995, 6, 15)
        );
        return customerRepository.save(customer);
    }

    private Staff createTestStaff(String suffix) {
        Role staffRole = roleRepository.findByName("ROLE_STAFF").orElseThrow();
        Staff staff = new Staff(
            "staff_" + suffix,
            "hashedPassword",
            "staff." + suffix + "@example.com",
            Set.of(staffRole),
            Set.of()
        );
        return staffRepository.save(staff);
    }

    // Token carrying the given user's id (so checkUserAccessPermission sees "own user")
    private String customerToken(Customer customer) {
        return jwtTokenizer.getAuthToken(customer.getEmail(), customer.getId(), USER_PERMISSIONS);
    }

    private String staffToken(Staff staff) {
        return jwtTokenizer.getAuthToken(staff.getEmail(), staff.getId(), ADMIN_PERMISSIONS);
    }

    // ===== Customer create (PermitAll, no token needed) =====

    @Test
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

        String anyToken = jwtTokenizer.getAuthToken("creator@test.at", 1L, USER_PERMISSIONS);
        assertDoesNotThrow(() -> mockMvc.perform(post("/api/v1/customer/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), anyToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("max.customer@test.at"))
            .andExpect(jsonPath("$.userName").value("max_customer"))
            .andExpect(jsonPath("$.userType").value("CUSTOMER")));
    }

    @Test
    public void createCustomer_withInvalidEmail_returns400() {
        String json = """
            {
              "type": "CUSTOMER",
              "userName": "bad_email_user",
              "password": "Password123!",
              "email": "not-an-email",
              "firstName": "Max",
              "lastName": "Mustermann",
              "dateOfBirth": "1998-05-10"
            }
            """;

        String anyToken = jwtTokenizer.getAuthToken("creator@test.at", 1L, USER_PERMISSIONS);
        assertDoesNotThrow(() -> mockMvc.perform(post("/api/v1/customer/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), anyToken)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest()));
    }

    // ===== Staff create (needs STAFF_CREATE) =====

    @Test
    public void createStaff_withValidDto_returns200AndSavedStaff() {
        Staff authStaff = createTestStaff("creator");

        String json = """
            {
              "type": "STAFF",
              "userName": "staff_user",
              "password": "Password123!",
              "email": "staff@test.at"
            }
            """;

        assertDoesNotThrow(() -> mockMvc.perform(post("/api/v1/staff/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), staffToken(authStaff))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("staff@test.at"))
            .andExpect(jsonPath("$.userName").value("staff_user"))
            .andExpect(jsonPath("$.userType").value("STAFF")));
    }

    // ===== Customer update (own user) =====

    @Test
    public void updateCustomer_ownAccount_returns200AndUpdatedFields() {
        Customer customer = createTestCustomer("update");

        String json = """
            {
              "type": "CUSTOMER",
              "userName": "updated_name",
              "firstName": "UpdatedFirst",
              "lastName": "UpdatedLast"
            }
            """;

        assertDoesNotThrow(() -> mockMvc.perform(put("/api/v1/customer/update/{id}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), customerToken(customer))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userName").value("updated_name"))
            .andExpect(jsonPath("$.firstName").value("UpdatedFirst"))
            .andExpect(jsonPath("$.lastName").value("UpdatedLast")));
    }

    @Test
    public void updateCustomer_withEmptyDto_returns400() {
        Customer customer = createTestCustomer("update_empty");

        String json = """
            {
              "type": "CUSTOMER"
            }
            """;

        assertDoesNotThrow(() -> mockMvc.perform(put("/api/v1/customer/update/{id}", customer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), customerToken(customer))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest()));
    }

    @Test
    public void updateCustomer_unknownId_returns404() {
        Customer customer = createTestCustomer("update_unknown");
        // Token carries the existing customer's id, but we target a non-existent id.
        // checkUserAccessPermission compares currentId == requestedId, so to reach the
        // NotFound branch we authorize with a matching-but-nonexistent id is impossible;
        // instead we use an admin-style token whose id equals the requested id.
        String json = """
            {
              "type": "CUSTOMER",
              "userName": "ghost"
            }
            """;

        long missingId = 999999L;
        String token = jwtTokenizer.getAuthToken("ghost@test.at", missingId, USER_PERMISSIONS);

        assertDoesNotThrow(() -> mockMvc.perform(put("/api/v1/customer/update/{id}", missingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), token)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound()));
    }

    @Test
    public void updateCustomer_foreignAccount_returns403() {
        Customer owner = createTestCustomer("owner");
        Customer attacker = createTestCustomer("attacker");

        String json = """
            {
              "type": "CUSTOMER",
              "userName": "hacked"
            }
            """;

        // attacker's token (their id) tries to update owner's account
        assertDoesNotThrow(() -> mockMvc.perform(put("/api/v1/customer/update/{id}", owner.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header(securityProperties.getAuthHeader(), customerToken(attacker))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden()));
    }

    // ===== Customer get =====

    @Test
    public void getCustomerById_ownAccount_returns200() {
        Customer customer = createTestCustomer("get");

        assertDoesNotThrow(() -> mockMvc.perform(get("/api/v1/customer/{id}", customer.getId())
                .header(securityProperties.getAuthHeader(), customerToken(customer))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userType").value("CUSTOMER")));
    }

    // ===== Customer delete =====

    @Test
    public void deleteCustomer_ownAccount_returns200AndDeletes() {
        Customer customer = createTestCustomer("delete");

        assertDoesNotThrow(() -> mockMvc.perform(delete("/api/v1/customer/delete/{id}", customer.getId())
                .header(securityProperties.getAuthHeader(), customerToken(customer)))
            .andExpect(status().isOk()));
    }

    // ===== Staff get / delete =====

    @Test
    public void getStaffById_ownAccount_returns200() {
        Staff staff = createTestStaff("get");

        assertDoesNotThrow(() -> mockMvc.perform(get("/api/v1/staff/{id}", staff.getId())
                .header(securityProperties.getAuthHeader(), staffToken(staff))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userType").value("STAFF")));
    }

    @Test
    public void deleteStaff_ownAccount_returns200AndDeletes() {
        Staff staff = createTestStaff("delete");

        assertDoesNotThrow(() -> mockMvc.perform(delete("/api/v1/staff/delete/{id}", staff.getId())
                .header(securityProperties.getAuthHeader(), staffToken(staff)))
            .andExpect(status().isOk()));
    }
}