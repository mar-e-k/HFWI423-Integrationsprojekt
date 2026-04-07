package com.example.application.api;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.goodsreceipts.GoodsReceipt;
import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.services.ArticleInfoService;
import com.example.application.services.GoodsReceiptService;
import com.example.application.services.KommissionService;
import com.example.application.services.StorageLocationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST-Endpunkte für JMeter-Lasttests.
 * Bildet dieselben DB-Zugriffe ab wie die Vaadin-Views,
 * ist aber per HTTP direkt testbar ohne Vaadin-Session.
 *
 * Basis-URL: /api/load
 */
@RestController
@RequestMapping("/api/load")
public class LoadTestController {

    private final ArticleInfoService articleInfoService;
    private final StorageLocationService storageLocationService;
    private final GoodsReceiptService goodsReceiptService;
    private final KommissionService kommissionService;

    public LoadTestController(ArticleInfoService articleInfoService,
                              StorageLocationService storageLocationService,
                              GoodsReceiptService goodsReceiptService,
                              KommissionService kommissionService) {
        this.articleInfoService = articleInfoService;
        this.storageLocationService = storageLocationService;
        this.goodsReceiptService = goodsReceiptService;
        this.kommissionService = kommissionService;
    }

    /** GET /api/load/articles?page=0&size=20
     *  Artikelliste paginiert – entspricht der LogisticMainView */
    @GetMapping("/articles")
    public Page<ArticleInfo> articles(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return articleInfoService.list(PageRequest.of(page, size), null);
    }

    /** GET /api/load/storage-locations
     *  Alle Lagerplätze – entspricht StorageLocationView */
    @GetMapping("/storage-locations")
    public List<StorageLocation> storageLocations() {
        return storageLocationService.findAll();
    }

    /** GET /api/load/goods-receipts
     *  Alle Wareneingänge – entspricht GoodsReceiptView */
    @GetMapping("/goods-receipts")
    public List<GoodsReceipt> goodsReceipts() {
        return goodsReceiptService.findAll();
    }

    /** GET /api/load/kommissionen
     *  Alle Kommissionen – entspricht OrderPickingView */
    @GetMapping("/kommissionen")
    public List<Kommission> kommissionen() {
        return kommissionService.getAlleKommissionen();
    }

    /** GET /api/load/health – einfacher Ping für JMeter-Warmup */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "logistik");
    }
}
