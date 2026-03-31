package fhdw.de.einkauf_service;

import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.entity.Supplier;
import fhdw.de.einkauf_service.entity.PaymentTerm;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
import fhdw.de.einkauf_service.repository.PaymentTermRepository;
import fhdw.de.einkauf_service.service.PurchaseOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PurchaseOrderServiceIntegrationTest {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PaymentTermRepository paymentTermRepository;

    @Autowired
    private fhdw.de.einkauf_service.config.ShoppingCartSession cartSession;

    private Long testArticleId;

    @BeforeEach
    public void setUp() {
        // Erstelle Testdaten: PaymentTerm, Supplier und Article
        PaymentTerm paymentTerm = new PaymentTerm();
        paymentTerm.setDefinition("Test Payment Term");
        paymentTerm = paymentTermRepository.save(paymentTerm);

        Supplier supplier = new Supplier();
        supplier.setName("Test Supplier");
        supplier.setActive(true);
        supplier.setCity("Test City");
        supplier.setCountry("Test Country");
        supplier.setStreet("Test Street");
        supplier.setHouseNumber("123");
        supplier.setZip("12345");
        supplier.setPhone("123456789");
        supplier.setPaymentTerm(paymentTerm);
        supplier = supplierRepository.save(supplier);

        Article article = new Article();
        article.setName("Test Article");
        article.setPurchasePrice(10.0);
        article.setAvailable(true);
        article.setMainSupplier(supplier);
        article.setManufacturer("Test Manufacturer");
        article.setArticleNumber("123456789012345678");
        article.setHeightCm(10.0);
        article.setWidthCm(10.0);
        article.setDepthCm(10.0);
        article.setSellingPrice(15.0);
        article.setStockLevel(100);
        article.setTaxRatePercent(19.0);
        article.setHasDeposit(false);
        article = articleRepository.save(article);
        testArticleId = article.getId();

        // Leere Warenkorb
        cartSession.clearCart();
    }

    @Test
    public void testGetOrderHistory() {
        // Teste das Abrufen der Bestellhistorie
        var orders = purchaseOrderService.getOrderHistory(new fhdw.de.einkauf_service.dto.OrderFilterDTO());
        assertNotNull(orders);
        // Weitere Assertions können hinzugefügt werden
    }

    @Test
    public void testCreateAndSendOrdersFromCart() {
        // Setze Warenkorb mit Test-Artikel
        cartSession.addItem(testArticleId, 5);

        // Erstelle Bestellung aus Warenkorb
        var responses = purchaseOrderService.createAndSendOrdersFromCart();
        assertNotNull(responses);
        assertFalse(responses.isEmpty());

        // Prüfe, ob Warenkorb geleert wurde
        assertTrue(cartSession.getItems().isEmpty());
    }

    @Test
    public void testGetOrderDetails() {
        // Erstelle eine Test-Bestellung zuerst
        cartSession.addItem(testArticleId, 2);
        var createdOrders = purchaseOrderService.createAndSendOrdersFromCart();
        assertFalse(createdOrders.isEmpty());

        Long orderId = createdOrders.get(0).id();
        var details = purchaseOrderService.getOrderDetails(orderId);
        assertNotNull(details);
        assertEquals(orderId, details.id());
    }

    @Test
    public void testReorder() {
        // Erstelle ursprüngliche Bestellung
        cartSession.addItem(testArticleId, 3);
        var originalOrders = purchaseOrderService.createAndSendOrdersFromCart();
        Long originalOrderId = originalOrders.get(0).id();

        // Reorder mit gleichen Items
        var items = originalOrders.get(0).items().stream()
                .map(item -> new fhdw.de.einkauf_service.dto.OrderItemRequestDTO(item.articleId(), item.quantity()))
                .toList();

        var reordered = purchaseOrderService.reorder(originalOrderId, items);
        assertNotNull(reordered);
        assertNotEquals(originalOrderId, reordered.id()); // Neue Bestellung
    }
}
