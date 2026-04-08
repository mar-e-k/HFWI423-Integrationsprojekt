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

    // =========================================================================
    // TODO: LogisticMainView – fehlende Endpunkte
    // =========================================================================

    // TODO: GET /api/load/articles/filter?name=&articleNumber=&minStock=&storageLocation=
    //       Artikel mit Filterparametern laden – entspricht der Suchfunktion in LogisticMainView
    //       Service: articleInfoService.list(pageable, filter)

    // TODO: POST /api/load/articles/{id}/stock
    //       Bestand eines Artikels ändern – entspricht StockChangeDialog in LogisticMainView
    //       Service: articleInfoService.applyStockChange() / updateStock()

    // TODO: PUT /api/load/articles/{id}/storage-location
    //       Lagerplatz eines Artikels zuweisen – entspricht Klick auf Lagerplatz-Badge
    //       Service: articleInfoService.updateStorageLocation(), storageLocationService.save()

    // =========================================================================
    // TODO: StorageLocationView – fehlende Endpunkte
    // =========================================================================

    // TODO: POST /api/load/storage-locations
    //       Neuen Lagerplatz anlegen – entspricht "+ Lagerplatz hinzufügen" Button
    //       Service: storageLocationService.saveWithDuplicateCheck()

    // TODO: PUT /api/load/storage-locations/{id}
    //       Lagerplatz bearbeiten – entspricht Edit-Dialog in StorageLocationView
    //       Service: storageLocationService.saveWithDuplicateCheck(), articleInfoService.updateStorageLocationForAll()

    // TODO: DELETE /api/load/storage-locations/{id}
    //       Lagerplatz löschen – entspricht Trash-Button in StorageLocationView
    //       Service: storageLocationService.delete()

    // TODO: POST /api/load/storage-locations/sync
    //       Status mit Artikeln synchronisieren – entspricht "Aktualisieren" Button
    //       Service: storageLocationService.syncStatusesWithArticles()

    // =========================================================================
    // TODO: GoodsReceiptView – fehlende Endpunkte
    // =========================================================================

    // TODO: POST /api/load/goods-receipts
    //       Wareneingang aus Restock-Orders erstellen – entspricht "Neuer Wareneingang" Dialog
    //       Service: goodsReceiptService.createFromRestockOrders()

    // TODO: POST /api/load/goods-receipts/{id}/items
    //       Artikel-Position zum Wareneingang hinzufügen – entspricht "Position hinzufügen"
    //       Service: goodsReceiptService.addItemToReceipt()

    // TODO: PUT /api/load/goods-receipts/{id}/items/{itemId}
    //       Position aktualisieren (Ist-Menge, Mängel) – entspricht "Änderungen speichern"
    //       Service: goodsReceiptService.updateItem()

    // TODO: PUT /api/load/goods-receipts/{id}/items/{itemId}/status
    //       Position freigeben oder sperren – entspricht "Freigeben" / "Sperren" Button
    //       Service: goodsReceiptService.setItemStatus(FREIGEGEBEN / GESPERRT)

    // TODO: POST /api/load/goods-receipts/{id}/complete
    //       Prüfung abschliessen – entspricht "Prüfung abschließen" Button
    //       Service: goodsReceiptService.completeInspection()

    // TODO: DELETE /api/load/goods-receipts/{id}
    //       Wareneingang löschen – entspricht "Löschen" Button
    //       Service: goodsReceiptService.deleteIfAllowed()

    // =========================================================================
    // TODO: OrderPickingView – fehlende Endpunkte
    // =========================================================================

    // TODO: POST /api/load/kommissionen/trigger
    //       Wöchentliche Kommissionierung auslösen – entspricht "Kommissionierung auslösen" Button
    //       Service: weeklyKommissionScheduler.createWeeklyKommissionen()

    // TODO: PUT /api/load/kommissionen/{id}/finish
    //       Kommission als erledigt markieren – entspricht "Finished" Checkbox
    //       Service: kommissionService.save(), articleInfoService.updateStock(), logisticEventPublisher.publishArticleDelivery()

    // TODO: PUT /api/load/kommissionen/{id}/items/{articleId}/quantity
    //       Realisierte Menge einer Position setzen – entspricht ComboBox in Detaildialog
    //       Service: messageLogisticRepository.save()

    // =========================================================================
    // TODO: Noch nicht abgedeckte Views
    // =========================================================================

    // TODO: GET /api/load/restock
    //       Artikel unter Mindestbestand laden – entspricht RestockView
    //       Service: restockService.getArticlesToRestock()

    // TODO: GET /api/load/stock-changes
    //       Lagerbestand-Änderungshistorie laden – entspricht StockChangeLogView
    //       Service: stockChangeLogService.findAll()

    // TODO: GET /api/load/new-articles
    //       Neue Artikel aus Kontingenten laden – entspricht NewArticlesView
    //       Service: articleSyncService.findNewArticlesFromContingents()

    // TODO: GET /api/load/messaging-events
    //       Messaging-Events laden – entspricht MessagingView
    //       Service: messagingEventService.findAll()
}
