package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.store.core.domain.store_stock.StoreStock;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStockService;
import de.fhdw.vendix.store.core.messaging.ArticleOrderMessagingService;
import de.fhdw.vendix.store.web.controller.dto.ArticleOrderRequest;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Test/admin controller that exposes HTTP endpoints for driving the
 * AMQP messaging pipeline from k6 load tests.
 *
 * <p><strong>Only active when the {@code prod} Spring profile is NOT set.</strong>
 * Remove {@code @Profile} or adjust the condition before deploying to production.
 *
 * <h3>Endpoints</h3>
 * <pre>
 *   POST /api/test/order           – publishes a normal ArticleOrderEvent
 *   POST /api/test/order/urgent    – publishes an urgent ArticleUrgentOrderEvent
 *   GET  /api/test/store-stock     – reads current stock for one article
 * </pre>
 *
 * <p>All endpoints sit under {@code /api/**} and therefore require a valid
 * JWT — the same token that k6's {@code setupAuth()} already obtains.
 */
@RestController
@RequestMapping("/api/test")
@Profile("!prod")
class TestOrderController {

    private final ArticleOrderMessagingService articleOrderMessagingService;
    private final StoreStockService storeStockService;

    TestOrderController(
            ArticleOrderMessagingService articleOrderMessagingService,
            StoreStockService storeStockService
    ) {
        this.articleOrderMessagingService = articleOrderMessagingService;
        this.storeStockService = storeStockService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Trigger endpoints — used by k6 to inject order events into AMQP
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Publishes a normal {@code ArticleOrderEvent} to RabbitMQ.
     *
     * <pre>
     * POST /api/test/order
     * { "storeId": 1, "articleId": 5, "amount": 10 }
     * </pre>
     */
    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> triggerOrder(
            @Valid @RequestBody ArticleOrderRequest request
    ) {
        articleOrderMessagingService.sendOrder(
                request.storeId(),
                request.articleId(),
                request.amount()
        );
        return ResponseEntity.ok(Map.of(
                "status",    "sent",
                "type",      "normal",
                "storeId",   request.storeId(),
                "articleId", request.articleId(),
                "amount",    request.amount()
        ));
    }

    /**
     * Publishes an urgent {@code ArticleUrgentOrderEvent} to RabbitMQ.
     *
     * <pre>
     * POST /api/test/order/urgent
     * { "storeId": 1, "articleId": 5, "amount": 10 }
     * </pre>
     */
    @PostMapping("/order/urgent")
    public ResponseEntity<Map<String, Object>> triggerUrgentOrder(
            @Valid @RequestBody ArticleOrderRequest request
    ) {
        articleOrderMessagingService.sendUrgentOrder(
                request.storeId(),
                request.articleId(),
                request.amount()
        );
        return ResponseEntity.ok(Map.of(
                "status",    "sent",
                "type",      "urgent",
                "storeId",   request.storeId(),
                "articleId", request.articleId(),
                "amount",    request.amount()
        ));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Verification endpoints — used by k6 to assert side-effects
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the current stock entry for a given store/article combination.
     * k6 uses this after JMeter has sent back {@code ArticleSentEvent}s to
     * verify that the store's inventory was actually raised.
     *
     * <pre>
     * GET /api/test/store-stock?storeId=1&articleId=5
     * </pre>
     *
     * Returns 404 if no stock record exists for that combination.
     */
    @GetMapping("/store-stock")
    public ResponseEntity<Map<String, Object>> getStoreStock(
            @RequestParam long storeId,
            @RequestParam long articleId
    ) {
        Optional<StoreStock> stock = storeStockService.findByStoreIdAndArticleId(storeId, articleId);
        return stock
                .map(s -> ResponseEntity.ok(Map.<String, Object>of(
                        "storeId",       s.getStoreId(),
                        "articleId",     s.getArticleId(),
                        "currentAmount", s.getCurrentAmount(),
                        "minAmount",     s.getPreferenceAmount().getMin(),
                        "avgAmount",     s.getPreferenceAmount().getAvg(),
                        "maxAmount",     s.getPreferenceAmount().getMax()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
