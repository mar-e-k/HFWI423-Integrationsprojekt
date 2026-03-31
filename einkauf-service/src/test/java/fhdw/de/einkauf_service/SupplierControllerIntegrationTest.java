package fhdw.de.einkauf_service;


import fhdw.de.einkauf_service.entity.PaymentTerm;
import fhdw.de.einkauf_service.repository.PaymentTermRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SupplierControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private PaymentTermRepository paymentTermRepository;

    private Long testPaymentTermId;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Erstelle Test-PaymentTerm für Supplier-Erstellung
        PaymentTerm paymentTerm = new PaymentTerm();
        paymentTerm.setDefinition("Test Payment Term for Suppliers");
        paymentTerm = paymentTermRepository.save(paymentTerm);
        testPaymentTermId = paymentTerm.getId();
    }

    @Test
    public void testCreateSupplier() throws Exception {
        String supplierJson = """
            {
                "name": "Test Supplier",
                "city": "Test City",
                "country": "Test Country",
                "street": "Test Street",
                "houseNumber": "123",
                "zip": "12345",
                "phone": "123456789",
                "email": "test@supplier.com",
                "paymentTermId": %d
            }
            """.formatted(testPaymentTermId);

        mockMvc.perform(post("/api/v1/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(supplierJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Supplier"));
    }

    @Test
    public void testGetAllSuppliers() throws Exception {
        mockMvc.perform(get("/api/v1/suppliers"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetSupplierById() throws Exception {
        // Erstelle zuerst einen Supplier
        String supplierJson = """
            {
                "name": "Supplier for Get",
                "city": "Test City",
                "country": "Test Country",
                "street": "Test Street",
                "houseNumber": "456",
                "zip": "67890",
                "phone": "987654321",
                "email": "get@supplier.com",
                "paymentTermId": %d
            }
            """.formatted(testPaymentTermId);

        var createdResult = mockMvc.perform(post("/api/v1/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(supplierJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Hole Supplier by ID
        Number supplierIdNum = com.jayway.jsonpath.JsonPath.read(createdResult, "$.id");
        Long supplierId = supplierIdNum.longValue();

        mockMvc.perform(get("/api/v1/suppliers/" + supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Supplier for Get"));
    }

    @Test
    public void testUpdateSupplier() throws Exception {
        // Erstelle Supplier
        String createJson = """
            {
                "name": "Supplier to Update",
                "city": "Old City",
                "country": "Old Country",
                "street": "Old Street",
                "houseNumber": "789",
                "zip": "10111",
                "phone": "111111111",
                "email": "update@supplier.com",
                "paymentTermId": %d
            }
            """.formatted(testPaymentTermId);

        var createdResult = mockMvc.perform(post("/api/v1/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number supplierIdNum = com.jayway.jsonpath.JsonPath.read(createdResult, "$.id");
        Long supplierId = supplierIdNum.longValue();

        // Update
        String updateJson = """
            {
                "name": "Updated Supplier",
                "city": "New City",
                "country": "New Country",
                "street": "New Street",
                "houseNumber": "999",
                "zip": "20222",
                "phone": "222222222",
                "email": "updated@supplier.com",
                "paymentTermId": %d
            }
            """.formatted(testPaymentTermId);

        mockMvc.perform(put("/api/v1/suppliers/" + supplierId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Supplier"));
    }

    @Test
    public void testDeleteSupplier() throws Exception {
        // Erstelle Supplier
        String supplierJson = """
            {
                "name": "Supplier to Delete",
                "city": "Delete City",
                "country": "Delete Country",
                "street": "Delete Street",
                "houseNumber": "000",
                "zip": "00000",
                "phone": "000000000",
                "email": "delete@supplier.com",
                "paymentTermId": %d
            }
            """.formatted(testPaymentTermId);

        var createdResult = mockMvc.perform(post("/api/v1/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(supplierJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number supplierIdNum = com.jayway.jsonpath.JsonPath.read(createdResult, "$.id");
        Long supplierId = supplierIdNum.longValue();

        // Lösche
        mockMvc.perform(delete("/api/v1/suppliers/" + supplierId))
                .andExpect(status().isNoContent());
    }
}
