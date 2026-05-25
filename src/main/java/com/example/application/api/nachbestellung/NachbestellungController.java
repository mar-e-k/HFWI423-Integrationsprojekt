package com.example.application.api.nachbestellung;

import com.example.application.data.articleInfo.RestockItem;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.data.restockorder.RestockOrderRepository;
import com.example.application.services.RestockOrderService;
import com.example.application.services.RestockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nachbestellungen")
@Tag(name = "Nachbestellungen", description = "Nachbestellung und Restock-Verwaltung")
public class NachbestellungController {

    private final RestockService restockService;
    private final RestockOrderService restockOrderService;
    private final RestockOrderRepository restockOrderRepository;

    public NachbestellungController(RestockService restockService,
                                    RestockOrderService restockOrderService,
                                    RestockOrderRepository restockOrderRepository) {
        this.restockService = restockService;
        this.restockOrderService = restockOrderService;
        this.restockOrderRepository = restockOrderRepository;
    }

    @Operation(summary = "Artikel unter Mindestbestand abrufen")
    @GetMapping
    public List<RestockItem> restock() {
        return restockService.getArticlesToRestock();
    }

    @Operation(summary = "Naechsten bestellbaren Artikel bestellen")
    @PostMapping("/approve-next")
    @Transactional
    public ResponseEntity<RestockOrder> approveNextRestock() {
        int orphansDeleted = restockOrderRepository.deleteOrphanedSimOrders();
        System.out.println("[approve-next] orphansDeleted=" + orphansDeleted);
        List<RestockItem> items = restockService.getArticlesToRestock();
        for (RestockItem item : items) {
            if (item.getOrderAmount() == null || item.getOrderAmount() <= 0) continue;
            if (restockOrderService.hasOpenOrderForArticle(item.getArticle())) continue;
            try {
                RestockOrder order = restockOrderService.approveOrderForLoadTest(item);
                return ResponseEntity.ok(order);
            } catch (Exception e) {
                System.out.println("[approve-next] SKIP " + item.getArticleNumber() + ": " + e.getMessage());
            }
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Alle nachbestellbaren Artikel genehmigen")
    @PostMapping("/approve-all")
    public ResponseEntity<Integer> approveAllRestock() {
        List<RestockItem> items = restockService.getArticlesToRestock();
        int approved = 0;
        for (RestockItem item : items) {
            if (item.getOrderAmount() == null || item.getOrderAmount() <= 0) continue;
            if (restockOrderService.hasOpenOrderForArticle(item.getArticle())) continue;
            try {
                restockOrderService.approveOrderForLoadTest(item);
                approved++;
            } catch (IllegalStateException ignored) {
            } catch (Exception e) {
                System.out.println("[approve-all] SKIP " + item.getArticleNumber() + ": " + e.getMessage());
            }
        }
        return ResponseEntity.ok(approved);
    }
}
