//package fhdw.de.einkauf_service.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import fhdw.de.einkauf_service.dto.ContactPersonRequestDTO;
//import fhdw.de.einkauf_service.dto.SupplierRequestDTO;
//import fhdw.de.einkauf_service.entity.PaymentTerm;
//import fhdw.de.einkauf_service.repository.PaymentTermRepository;
//import fhdw.de.einkauf_service.repository.SupplierRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.UUID;
//
//import static org.hamcrest.Matchers.hasSize;
//import static org.hamcrest.Matchers.is;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
///**
// * Integrationstest für SupplierController
// * CRUD + ManyToMany ContactPeople + PaymentTerm-Bezug
// */
//@SpringBootTest(
//        classes = fhdw.de.einkauf_service.PurchaseServiceApplication.class,
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
//)
//@AutoConfigureMockMvc
//@Transactional
//@ActiveProfiles("test")
//public class SupplierControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private PaymentTermRepository paymentTermRepository;
//
//    private Long paymentTermId;
//
//    private static final String API_URL = "/api/v1/suppliers";
//
//    @Autowired
//    private SupplierRepository supplierRepository;
//
//    /**
//     * Stellt sicher, dass ein eindeutiges PaymentTerm existiert,
//     * das über die gesamte Test-Transaktion hinweg verwendet werden kann.
//     */
//    @BeforeEach
//    void setUp() {
//        //Bereinigen
//        supplierRepository.deleteAll();
//
//        List<PaymentTerm> terms = paymentTermRepository.findAll();
//        if (terms.isEmpty() || terms.stream().noneMatch(t -> t.getDefinition().contains("TEST-TERM"))) {
//            // Erstellen eines eindeutigen PaymentTerm, um DataIntegrityViolation zu vermeiden
//            PaymentTerm pt = new PaymentTerm();
//            // Eindeutige Definition (z.B. mit UUID)
//            pt.setDefinition("TEST-TERM-" + UUID.randomUUID().toString().substring(0, 8));
//            pt.setDescription("Zahlung innerhalb von 30 Tagen ohne Abzug (Test-Setup)");
//            paymentTermId = paymentTermRepository.save(pt).getId();
//        } else {
//            // Ein vorhandenes TEST-TERM verwenden
//            paymentTermId = terms.stream().filter(t -> t.getDefinition().contains("TEST-TERM")).findFirst().get().getId();
//        }
//    }
//
//    /**
//     * Helferfunktion: Erstellt eine Anfrage mit eindeutigen Werten für Unique Constraints.
//     */
//    private SupplierRequestDTO createValidSupplierRequest() {
//        String uniqueId = UUID.randomUUID().toString();
//        String uniqueSupplierEmail = "kontakt_" + uniqueId.substring(0, 8) + "@supplier-test.de";
//
//        ContactPersonRequestDTO contact1 = new ContactPersonRequestDTO();
//        contact1.setFirstName("Anna");
//        contact1.setLastName("Mueller");
//        contact1.setEmail("anna." + uniqueId.substring(0, 4) + "@contact-test.com"); // Eindeutige E-Mail
//        contact1.setPhone("01234-56789");
//        contact1.setRole("Vertrieb");
//
//        ContactPersonRequestDTO contact2 = new ContactPersonRequestDTO();
//        contact2.setFirstName("Peter");
//        contact2.setLastName("Schmidt");
//        contact2.setEmail("peter." + uniqueId.substring(4, 8) + "@contact-test.com"); // Eindeutige E-Mail
//        contact2.setPhone("09876-54321");
//        contact2.setRole("Einkauf");
//
//        SupplierRequestDTO dto = new SupplierRequestDTO();
//        dto.setName("Test Supplier GmbH - " + uniqueId); // Eindeutiger Name, falls Unique Constraint existiert
//        dto.setStreet("Musterstraße");
//        dto.setHouseNumber("12a");
//        dto.setZip("12345");
//        dto.setCity("Köln");
//        dto.setCountry("Deutschland");
//        dto.setEmail(uniqueSupplierEmail); // Eindeutige E-Mail
//        dto.setPhone("0221-123456");
//        dto.setPaymentTermId(paymentTermId);
//        dto.setContactPeople(List.of(contact1, contact2));
//
//        return dto;
//    }
//
//    // CREATE
//    @Test
//    void shouldCreateSupplierSuccessfully() throws Exception {
//        SupplierRequestDTO dto = createValidSupplierRequest();
//
//        mockMvc.perform(post(API_URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isCreated()) // Erwartet 201
//                .andExpect(jsonPath("$.name", is(dto.getName())))
//                .andExpect(jsonPath("$.contactPeople", hasSize(2)))
//                .andExpect(jsonPath("$.paymentTerm.id", is(paymentTermId.intValue())));
//    }
//
//    // GET BY ID
//    @Test
//    void shouldGetSupplierById() throws Exception {
//        SupplierRequestDTO dto = createValidSupplierRequest();
//        String response = mockMvc.perform(post(API_URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isCreated())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        Long createdId = objectMapper.readTree(response).get("id").asLong();
//
//        mockMvc.perform(get(API_URL + "/" + createdId)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk()) // Erwartet 200
//                .andExpect(jsonPath("$.id", is(createdId.intValue())))
//                .andExpect(jsonPath("$.contactPeople", hasSize(2)))
//                .andExpect(jsonPath("$.paymentTerm.id", is(paymentTermId.intValue())));
//    }
//
//    // GET ALL
//    @Test
//    void shouldGetAllSuppliers() throws Exception {
//        // Erstellung der Lieferanten mit eindeutigen Namen/E-Mails
//        SupplierRequestDTO supplier1 = createValidSupplierRequest();
//        SupplierRequestDTO supplier2 = createValidSupplierRequest();
//
//        mockMvc.perform(post(API_URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(supplier1)))
//                .andExpect(status().isCreated());
//
//        mockMvc.perform(post(API_URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(supplier2)))
//                .andExpect(status().isCreated());
//
//        mockMvc.perform(get(API_URL)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                // Hier korrigiert, um nur die erstellten 2 Lieferanten zu erwarten
//                .andExpect(jsonPath("$", hasSize(2)));
//    }
//
//    // UPDATE
//    @Test
//    void shouldUpdateSupplierSuccessfully() throws Exception {
//        SupplierRequestDTO dto = createValidSupplierRequest();
//        String response = mockMvc.perform(post(API_URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isCreated())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        Long createdId = objectMapper.readTree(response).get("id").asLong();
//
//        // Update-DTO mit neuen, eindeutigen Werten erstellen
//        SupplierRequestDTO updateDto = createValidSupplierRequest(); // Neue eindeutige E-Mails generieren
//        updateDto.setCity("Düsseldorf");
//        updateDto.setPhone("0221-987654");
//        updateDto.setName(dto.getName()); // Originalnamen beibehalten (wichtig, falls der Name Unique ist)
//
//        mockMvc.perform(put(API_URL + "/" + createdId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateDto)))
//                .andExpect(status().isOk()) // Erwartet 200
//                .andExpect(jsonPath("$.city", is("Düsseldorf")))
//                .andExpect(jsonPath("$.phone", is("0221-987654")));
//    }
//
//    // DELETE
//    @Test
//    void shouldDeleteSupplierSuccessfully() throws Exception {
//        SupplierRequestDTO dto = createValidSupplierRequest();
//        String response = mockMvc.perform(post(API_URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isCreated())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        Long createdId = objectMapper.readTree(response).get("id").asLong();
//
//        mockMvc.perform(delete(API_URL + "/" + createdId))
//                .andExpect(status().isNoContent()); // Erwartet 204
//
//        mockMvc.perform(get(API_URL + "/" + createdId))
//                .andExpect(status().isNotFound());
//    }
//
//    // ERROR CASE
//    @Test
//    void shouldReturnNotFoundForNonExistingSupplier() throws Exception {
//        mockMvc.perform(get(API_URL + "/9999")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//    }
//}