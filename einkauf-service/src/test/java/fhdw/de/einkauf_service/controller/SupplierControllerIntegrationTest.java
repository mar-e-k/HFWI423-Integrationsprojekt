package fhdw.de.einkauf_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fhdw.de.einkauf_service.dto.ContactPersonRequestDTO;
import fhdw.de.einkauf_service.dto.SupplierRequestDTO;
import fhdw.de.einkauf_service.entity.PaymentTerm;
import fhdw.de.einkauf_service.repository.PaymentTermRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstest für SupplierController
 * CRUD + ManyToMany ContactPeople + PaymentTerm-Bezug
 */
@SpringBootTest(
        classes = fhdw.de.einkauf_service.PurchaseServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class SupplierControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentTermRepository paymentTermRepository;

    private Long paymentTermId;

    private static final String API_URL = "/api/v1/suppliers";

    @BeforeEach
    void setUp() {
        // Sicherstellen, dass ein PaymentTerm existiert
        List<PaymentTerm> terms = paymentTermRepository.findAll();
        if (terms.isEmpty()) {
            PaymentTerm pt = new PaymentTerm();
            pt.setDefinition("30 Tage netto");
            pt.setDescription("Zahlung innerhalb von 30 Tagen ohne Abzug");
            paymentTermId = paymentTermRepository.save(pt).getId();
        } else {
            paymentTermId = terms.get(0).getId();
        }
    }

    private SupplierRequestDTO createValidSupplierRequest() {
        ContactPersonRequestDTO contact1 = new ContactPersonRequestDTO();
        contact1.setFirstName("Anna");
        contact1.setLastName("Müller");
        contact1.setEmail("anna.mueller@example.com");
        contact1.setPhone("01234-56789");
        contact1.setRole("Vertrieb");

        ContactPersonRequestDTO contact2 = new ContactPersonRequestDTO();
        contact2.setFirstName("Peter");
        contact2.setLastName("Schmidt");
        contact2.setEmail("peter.schmidt@example.com");
        contact2.setPhone("09876-54321");
        contact2.setRole("Einkauf");

        SupplierRequestDTO dto = new SupplierRequestDTO();
        dto.setName("Test Supplier GmbH");
        dto.setStreet("Musterstraße");
        dto.setHouseNumber("12a");
        dto.setZip("12345");
        dto.setCity("Köln");
        dto.setCountry("Deutschland");
        dto.setEmail("kontakt@supplier.de");
        dto.setPhone("0221-123456");
        dto.setPaymentTermId(paymentTermId);
        dto.setContactPeople(List.of(contact1, contact2));

        return dto;
    }

    // CREATE
    @Test
    void shouldCreateSupplierSuccessfully() throws Exception {
        SupplierRequestDTO dto = createValidSupplierRequest();

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(dto.getName())))
                .andExpect(jsonPath("$.contactPeople", hasSize(2)))
                .andExpect(jsonPath("$.paymentTerm.id", is(paymentTermId.intValue())));
    }

    // GET BY ID
    @Test
    void shouldGetSupplierById() throws Exception {
        SupplierRequestDTO dto = createValidSupplierRequest();
        String response = mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get(API_URL + "/" + createdId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdId.intValue())))
                .andExpect(jsonPath("$.contactPeople", hasSize(2)))
                .andExpect(jsonPath("$.paymentTerm.id", is(paymentTermId.intValue())));
    }

    // GET ALL
    @Test
    void shouldGetAllSuppliers() throws Exception {
        SupplierRequestDTO supplier1 = createValidSupplierRequest();
        supplier1.setName("Supplier A");
        SupplierRequestDTO supplier2 = createValidSupplierRequest();
        supplier2.setName("Supplier B");

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplier1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplier2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(API_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    // UPDATE
    @Test
    void shouldUpdateSupplierSuccessfully() throws Exception {
        SupplierRequestDTO dto = createValidSupplierRequest();
        String response = mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdId = objectMapper.readTree(response).get("id").asLong();

        dto.setCity("Düsseldorf");
        dto.setPhone("0221-987654");

        mockMvc.perform(put(API_URL + "/" + createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city", is("Düsseldorf")))
                .andExpect(jsonPath("$.phone", is("0221-987654")));
    }

    // DELETE
    @Test
    void shouldDeleteSupplierSuccessfully() throws Exception {
        SupplierRequestDTO dto = createValidSupplierRequest();
        String response = mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete(API_URL + "/" + createdId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API_URL + "/" + createdId))
                .andExpect(status().isNotFound());
    }

    // ERROR CASE
    @Test
    void shouldReturnNotFoundForNonExistingSupplier() throws Exception {
        mockMvc.perform(get(API_URL + "/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
