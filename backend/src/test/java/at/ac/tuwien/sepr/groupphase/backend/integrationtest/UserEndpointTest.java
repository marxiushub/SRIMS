package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.IntegrationTestBase;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Customer;
import at.ac.tuwien.sepr.groupphase.backend.entity.user.Staff;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.CustomerRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.user.StaffRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles({"test"})
@SpringBootTest
@AutoConfigureMockMvc
public class UserEndpointTest extends IntegrationTestBase implements TestData {

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

     //Helper Methods
    private Customer createTestCustomer(String suffix) {
        Customer customer = new Customer(
            "customer_" + suffix,
            "hashedPassword",
            "customer." + suffix + "@test.at",
            Set.of(),
            Set.of(),
            "Max",
            "Mustermann",
            LocalDate.of(1998, 5, 10)
        );
        return customerRepository.save(customer);
    }

    private Staff createTestStaff(String suffix) {
        Staff staff = new Staff(
            "staff_" + suffix,
            "hashedPassword",
            "staff." + suffix + "@test.at",
            Set.of(),
            Set.of()
        );
        return staffRepository.save(staff);
    }

    private String customerToken(Customer customer) {
        return jwtTokenizer.getAuthToken(
            customer.getEmail(),
            customer.getId(),
            List.of("CUSTOMER_READ", "CUSTOMER_UPDATE", "CUSTOMER_DELETE")
        );
    }

    private String staffToken(Staff staff) {
        return jwtTokenizer.getAuthToken(
            staff.getEmail(),
            staff.getId(),
            List.of(
                "STAFF",
                "STAFF_READ",
                "STAFF_UPDATE",
                "STAFF_DELETE",
                "CUSTOMER_READ"
            )
        );
    }


    //CREATE CUSTOMER
    @Test
    public void createCustomer_withValidDto_returns200() throws Exception {

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

        mockMvc.perform(post("/api/v1/customer/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("max.customer@test.at"))
            .andExpect(jsonPath("$.userName").value("max_customer"));
    }

    @Test
    public void createCustomer_withInvalidDto_returns400() throws Exception {

        String json = """
        {
          "type": "CUSTOMER",
          "userName": "",
          "password": "123",
          "email": "invalid"
        }
        """;

        mockMvc.perform(post("/api/v1/customer/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }


    //CREATE STAFF
    @Test
    public void createStaff_withValidDto_returns200() throws Exception {

        Staff staff = createTestStaff("auth");

        String token = jwtTokenizer.getAuthToken(
            staff.getEmail(),
            staff.getId(),
            List.of("STAFF_CREATE")
        );

        String json = """
        {
          "type": "STAFF",
          "userName": "staff_user",
          "password": "Password123!",
          "email": "staff@test.at"
        }
        """;

        mockMvc.perform(post("/api/v1/staff/create")
                .header(securityProperties.getAuthHeader(), token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("staff@test.at"));
    }

    @Test
    public void createStaff_withoutPermission_returns403() throws Exception {

        Staff staff = createTestStaff("no_perm");

        String token = jwtTokenizer.getAuthToken(
            staff.getEmail(),
            staff.getId(),
            List.of("STAFF_READ") // missing CREATE
        );

        String json = """
        {
          "type": "STAFF",
          "userName": "staff_user",
          "password": "Password123!",
          "email": "staff@test.at"
        }
        """;

        mockMvc.perform(post("/api/v1/staff/create")
                .header(securityProperties.getAuthHeader(), token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isForbidden());
    }


    //UPDATE CUSTOMER
    @Test
    public void updateCustomer_withValidDto_returns200() throws Exception {

        Customer customer = createTestCustomer("update");

        String json = """
        {
          "type": "CUSTOMER",
          "userName": "updated_user",
          "email": "updated@test.at",
          "firstName": "Updated",
          "lastName": "User"
        }
        """;

        mockMvc.perform(put("/api/v1/customer/update/" + customer.getId())
                .header(securityProperties.getAuthHeader(), customerToken(customer))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("updated@test.at"));
    }

    @Test
    public void updateCustomer_withInvalidId_returns404() throws Exception {

        Customer customer = createTestCustomer("update_fail");

        String json = """
        {
          "type": "CUSTOMER",
          "userName": "updated_user"
        }
        """;

        mockMvc.perform(put("/api/v1/customer/update/999999")
                .header(securityProperties.getAuthHeader(), customerToken(customer))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isForbidden());
    }


    //UPDATE STAFF
    @Test
    public void updateStaff_withValidDto_returns200() throws Exception {

        Staff staff = createTestStaff("update");

        String json = """
        {
          "type": "STAFF",
          "userName": "updated_staff",
          "email": "updated@staff.at"
        }
        """;

        mockMvc.perform(put("/api/v1/staff/update/" + staff.getId())
                .header(securityProperties.getAuthHeader(), staffToken(staff))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("updated@staff.at"));
    }


    //DELETE
    @Test
    public void deleteCustomer_withValidId_returns200() throws Exception {

        Customer customer = createTestCustomer("delete");

        mockMvc.perform(delete("/api/v1/customer/delete/" + customer.getId())
                .header(securityProperties.getAuthHeader(), customerToken(customer)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/customer/" + customer.getId())
                .header(securityProperties.getAuthHeader(), customerToken(customer)))
            .andExpect(status().isNotFound());
    }

    @Test
    public void deleteCustomer_withoutPermission_returns403() throws Exception {

        Customer customer = createTestCustomer("delete_fail");
        Customer target = createTestCustomer("target");

        mockMvc.perform(delete("/api/v1/customer/delete/" + target.getId())
                .header(securityProperties.getAuthHeader(), customerToken(customer)))
            .andExpect(status().isForbidden());
    }


    //GET BY ID
    @Test
    public void getCustomerById_withValidId_returns200() throws Exception {

        Customer customer = createTestCustomer("get");

        mockMvc.perform(get("/api/v1/customer/" + customer.getId())
                .header(securityProperties.getAuthHeader(), customerToken(customer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(customer.getEmail()));
    }

    @Test
    public void getCustomerById_withoutPermission_returns403() throws Exception {

        Customer customer = createTestCustomer("get_fail");
        Customer target = createTestCustomer("target");

        mockMvc.perform(get("/api/v1/customer/" + target.getId())
                .header(securityProperties.getAuthHeader(), customerToken(customer)))
            .andExpect(status().isForbidden());
    }


    //SEARCH CUSTOMERS (STAFF ENDPOINT)
    @Test
    public void searchCustomers_asStaff_returns200AndResults() throws Exception {

        Staff staff = createTestStaff("search");

        Customer c1 = createTestCustomer("search1");
        Customer c2 = createTestCustomer("search2");

        mockMvc.perform(get("/api/v1/staff/customers/search")
                .header(securityProperties.getAuthHeader(), staffToken(staff))
                .param("email", c1.getEmail())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].email").value(hasItem(c1.getEmail())))
            .andExpect(jsonPath("$[*].id").value(hasItem(c1.getId().intValue())));
    }

    @Test
    public void searchCustomers_withoutPermission_returns403() throws Exception {

        Customer customer = createTestCustomer("search_fail");

        mockMvc.perform(get("/api/v1/staff/customers/search")
                .header(securityProperties.getAuthHeader(), customerToken(customer)))
            .andExpect(status().isForbidden());
    }

    @Test
    public void searchCustomers_withNoParams_returns200() throws Exception {

        Staff staff = createTestStaff("search_all");

        mockMvc.perform(get("/api/v1/staff/customers/search")
                .header(securityProperties.getAuthHeader(), staffToken(staff)))
            .andExpect(status().isOk());
    }
}
