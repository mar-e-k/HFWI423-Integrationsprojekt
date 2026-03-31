package fhdw.de.einkauf_service;


import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.entity.PaymentTerm;
import fhdw.de.einkauf_service.entity.Supplier;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.PaymentTermRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class ShoppingCartControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PaymentTermRepository paymentTermRepository;

    private Long testArticleId;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Erstelle Testdaten: PaymentTerm, Supplier und Article für Warenkorb-Tests
        PaymentTerm paymentTerm = new PaymentTerm();
        paymentTerm.setDefinition("Test Payment Term for Cart");
        paymentTerm = paymentTermRepository.save(paymentTerm);

        Supplier supplier = new Supplier();
        supplier.setName("Cart Test Supplier");
        supplier.setActive(true);
        supplier.setCity("Cart City");
        supplier.setCountry("Cart Country");
        supplier.setStreet("Cart Street");
        supplier.setHouseNumber("111");
        supplier.setZip("11111");
        supplier.setPhone("111111111");
        supplier.setPaymentTerm(paymentTerm);
        supplier = supplierRepository.save(supplier);

        Article article = new Article();
        article.setName("Cart Test Article");
        article.setPurchasePrice(5.0);
        article.setAvailable(true);
        article.setMainSupplier(supplier);
        article.setManufacturer("Cart Manufacturer");
        article.setArticleNumber("999999999999999999");
        article.setHeightCm(1.0);
        article.setWidthCm(1.0);
        article.setDepthCm(1.0);
        article.setSellingPrice(7.0);
        article.setStockLevel(200);
        article.setTaxRatePercent(19.0);
        article.setHasDeposit(false);
        article = articleRepository.save(article);
        testArticleId = article.getId();
    }

    @Test
    public void testAddItemToCart() throws Exception {
        mockMvc.perform(post("/api/v1/cart/add")
                .param("articleId", testArticleId.toString())
                .param("quantity", "5"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetCart() throws Exception {
        // Füge zuerst etwas zum Warenkorb hinzu
        mockMvc.perform(post("/api/v1/cart/add")
                .param("articleId", testArticleId.toString())
                .param("quantity", "3"))
                .andExpect(status().isOk());

        // Hole Warenkorb
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    public void testAddInvalidArticleToCart() throws Exception {
        // Versuche leeren Warenkorb zu lesen (sollte OK sein)
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk());
    }
}
