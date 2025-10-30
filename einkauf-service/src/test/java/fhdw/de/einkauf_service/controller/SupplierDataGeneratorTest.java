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
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstest für Supplier inkl. 3 Kontaktpersonen
 * mit persistierten Daten (keine Rollbacks)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SupplierDataGeneratorTest {

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

    private SupplierRequestDTO createSupplierRequest() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        ContactPersonRequestDTO contact1 = new ContactPersonRequestDTO();
        contact1.setFirstName("Anna-" + suffix);
        contact1.setLastName("Müller-" + suffix);
        contact1.setEmail("anna.mueller." + suffix + "@example.com");
        contact1.setPhone("01234-56789");
        contact1.setRole("Vertrieb");

        ContactPersonRequestDTO contact2 = new ContactPersonRequestDTO();
        contact2.setFirstName("Peter-" + suffix);
        contact2.setLastName("Schmidt-" + suffix);
        contact2.setEmail("peter.schmidt." + suffix + "@example.com");
        contact2.setPhone("09876-54321");
        contact2.setRole("Einkauf");

        ContactPersonRequestDTO contact3 = new ContactPersonRequestDTO();
        contact3.setFirstName("Laura-" + suffix);
        contact3.setLastName("Becker-" + suffix);
        contact3.setEmail("laura.becker." + suffix + "@example.com");
        contact3.setPhone("01111-22222");
        contact3.setRole("Support");

        SupplierRequestDTO dto = new SupplierRequestDTO();
        dto.setName("Test Supplier " + suffix);
        dto.setStreet("Musterstraße");
        dto.setHouseNumber("12a");
        dto.setZip("12345");
        dto.setCity("Köln");
        dto.setCountry("Deutschland");
        dto.setEmail("kontakt." + suffix + "@supplier.de");
        dto.setPhone("0221-123456");
        dto.setPaymentTermId(paymentTermId);
        dto.setContactPeople(List.of(contact1, contact2, contact3));

        return dto;
    }

    @Test
    @Commit
    void shouldCreateSupplierWithThreeContacts() throws Exception {
        SupplierRequestDTO dto = createSupplierRequest();

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(dto.getName())))
                .andExpect(jsonPath("$.contactPeople", hasSize(3)))
                .andExpect(jsonPath("$.paymentTerm.id", is(paymentTermId.intValue())));
    }
}
