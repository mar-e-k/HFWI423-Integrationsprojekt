package fhdw.de.einkauf_service;

import fhdw.de.einkauf_service.entity.PaymentTerm;
import fhdw.de.einkauf_service.entity.Supplier;
import fhdw.de.einkauf_service.repository.PaymentTermRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
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
public class ArticleControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PaymentTermRepository paymentTermRepository;

    private Long testSupplierId;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Erstelle Test-Supplier für Article-Erstellung
        PaymentTerm paymentTerm = new PaymentTerm();
        paymentTerm.setDefinition("Test Payment Term");
        paymentTerm = paymentTermRepository.save(paymentTerm);

        Supplier supplier = new Supplier();
        supplier.setName("Test Supplier for Articles");
        supplier.setActive(true);
        supplier.setCity("Test City");
        supplier.setCountry("Test Country");
        supplier.setStreet("Test Street");
        supplier.setHouseNumber("123");
        supplier.setZip("12345");
        supplier.setPhone("123456789");
        supplier.setPaymentTerm(paymentTerm);
        supplier = supplierRepository.save(supplier);
        testSupplierId = supplier.getId();
    }

    @Test
    public void testCreateArticle() throws Exception {
        String articleJson = """
            {
                "name": "Test Article",
                "manufacturer": "Test Manufacturer",
                "articleNumber": "123456789012345678",
                "purchasePrice": 10.0,
                "sellingPrice": 15.0,
                "stockLevel": 100,
                "taxRatePercent": 19.0,
                "heightCm": 10.0,
                "widthCm": 10.0,
                "depthCm": 10.0,
                "hasDeposit": false,
                "isAvailable": true,
                "mainSupplierId": %d,
                "supplierIds": [%d],
                "categoryIds": []
            }
            """.formatted(testSupplierId, testSupplierId);

        mockMvc.perform(post("/api/v1/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(articleJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Article"));
    }

    @Test
    public void testGetArticles() throws Exception {
        mockMvc.perform(get("/api/v1/articles"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetArticleById() throws Exception {
        // Erstelle zuerst einen Artikel
        String articleJson = """
            {
                "name": "Test Article for Get",
                "manufacturer": "Test Manufacturer",
                "articleNumber": "987654321098765432",
                "purchasePrice": 20.0,
                "sellingPrice": 25.0,
                "stockLevel": 50,
                "taxRatePercent": 19.0,
                "heightCm": 5.0,
                "widthCm": 5.0,
                "depthCm": 5.0,
                "hasDeposit": false,
                "isAvailable": true,
                "mainSupplierId": %d,
                "supplierIds": [%d],
                "categoryIds": []
            }
            """.formatted(testSupplierId, testSupplierId);

        var createdResult = mockMvc.perform(post("/api/v1/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(articleJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extrahiere ID aus Response mit JsonPath
        Number createdIdNum = com.jayway.jsonpath.JsonPath.read(createdResult, "$.id");
        Long createdId = createdIdNum.longValue();

        mockMvc.perform(get("/api/v1/articles/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Article for Get"));
    }

    @Test
    public void testUpdateArticle() throws Exception {
        // Erstelle Artikel
        String createJson = """
            {
                "name": "Article to Update",
                "manufacturer": "Test Manufacturer",
                "articleNumber": "111111111111111111",
                "purchasePrice": 30.0,
                "sellingPrice": 35.0,
                "stockLevel": 75,
                "taxRatePercent": 19.0,
                "heightCm": 15.0,
                "widthCm": 15.0,
                "depthCm": 15.0,
                "hasDeposit": false,
                "isAvailable": true,
                "mainSupplierId": %d,
                "supplierIds": [%d],
                "categoryIds": []
            }
            """.formatted(testSupplierId, testSupplierId);

        var createdResult = mockMvc.perform(post("/api/v1/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number articleIdNum = com.jayway.jsonpath.JsonPath.read(createdResult, "$.id");
        Long articleId = articleIdNum.longValue();

        // Update
        String updateJson = """
            {
                "name": "Updated Article",
                "manufacturer": "Updated Manufacturer",
                "articleNumber": "111111111111111111",
                "purchasePrice": 35.0,
                "sellingPrice": 40.0,
                "stockLevel": 80,
                "taxRatePercent": 19.0,
                "heightCm": 20.0,
                "widthCm": 20.0,
                "depthCm": 20.0,
                "hasDeposit": false,
                "isAvailable": true,
                "mainSupplierId": %d,
                "supplierIds": [%d],
                "categoryIds": []
            }
            """.formatted(testSupplierId, testSupplierId);

        mockMvc.perform(put("/api/v1/articles/" + articleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Article"));
    }

    @Test
    public void testDeleteArticle() throws Exception {
        // Erstelle Artikel
        String articleJson = """
            {
                "name": "Article to Delete",
                "manufacturer": "Test Manufacturer",
                "articleNumber": "222222222222222222",
                "purchasePrice": 40.0,
                "sellingPrice": 45.0,
                "stockLevel": 25,
                "taxRatePercent": 19.0,
                "heightCm": 25.0,
                "widthCm": 25.0,
                "depthCm": 25.0,
                "hasDeposit": false,
                "isAvailable": true,
                "mainSupplierId": %d,
                "supplierIds": [%d],
                "categoryIds": []
            }
            """.formatted(testSupplierId, testSupplierId);

        var createdResult = mockMvc.perform(post("/api/v1/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(articleJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number articleIdNum = com.jayway.jsonpath.JsonPath.read(createdResult, "$.id");
        Long articleId = articleIdNum.longValue();

        // Lösche
        mockMvc.perform(delete("/api/v1/articles/" + articleId))
                .andExpect(status().isNoContent());
    }
}
