package com.example.application.api.load;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.RestockItem;
import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.data.stockChangeLog.StockChangeLog;
import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.services.ArticleSyncService;
import com.example.application.services.MessagingEventService;
import com.example.application.services.NewArticleCandidate;
import com.example.application.services.RestockOrderService;
import com.example.application.services.RestockService;
import com.example.application.services.StockChangeLogService;
import com.example.application.services.StorageLocationService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Lasttest-Endpunkte für Views ohne eigene Kategorie:
 * RestockView, StockChangeLogView, NewArticlesView, MessagingView.
 * Basis-URL: /api/load
 */
@RestController
@RequestMapping("/api/load")
public class LoadTestMiscController {

    private final RestockService restockService;
    private final RestockOrderService restockOrderService;
    private final StockChangeLogService stockChangeLogService;
    private final ArticleSyncService articleSyncService;
    private final MessagingEventService messagingEventService;
    private final StorageLocationService storageLocationService;

    public LoadTestMiscController(RestockService restockService,
                                   RestockOrderService restockOrderService,
                                   StockChangeLogService stockChangeLogService,
                                   ArticleSyncService articleSyncService,
                                   MessagingEventService messagingEventService,
                                   StorageLocationService storageLocationService) {
        this.restockService = restockService;
        this.restockOrderService = restockOrderService;
        this.stockChangeLogService = stockChangeLogService;
        this.articleSyncService = articleSyncService;
        this.messagingEventService = messagingEventService;
        this.storageLocationService = storageLocationService;
    }

    /** GET /api/load/restock – Artikel unter Mindestbestand */
    @GetMapping("/restock")
    public List<RestockItem> restock() {
        return restockService.getArticlesToRestock();
    }

    /**
     * POST /api/load/restock/approve-next
     * Bestellt den naechsten bestellbaren Artikel aus der Nachbestellliste.
     * Vereinfacht fuer den Lasttest: keine Kontingent-Pruefung.
     * 200 + RestockOrder  → Bestellung erfolgreich angelegt
     * 204 No Content      → kein bestellbarer Artikel vorhanden
     */
    @PostMapping("/restock/approve-next")
    public ResponseEntity<RestockOrder> approveNextRestock() {
        List<RestockItem> items = restockService.getArticlesToRestock();
        for (RestockItem item : items) {
            if (item.getOrderAmount() == null || item.getOrderAmount() <= 0) continue;
            if (restockOrderService.hasOpenOrderForArticle(item.getArticle())) continue;
            try {
                RestockOrder order = restockOrderService.approveOrderForLoadTest(item);
                return ResponseEntity.ok(order);
            } catch (Exception e) {
                // naechsten Kandidaten probieren
            }
        }
        return ResponseEntity.noContent().build();
    }

    /** GET /api/load/stock-changes – Lagerbestand-Aenderungshistorie */
    @GetMapping("/stock-changes")
    public List<StockChangeLog> stockChanges() {
        return stockChangeLogService.findAll(Sort.by(Sort.Direction.DESC, "changedAt"));
    }

    /**
     * GET /api/load/new-articles – neue Artikel aus Kontingenten
     * ?lasttest=true  → liest aus contingent_lasttest (SIM-Artikel)
     * ?lasttest=false → liest aus echtem contingent (Produktion, Standard)
     */
    @GetMapping("/new-articles")
    public List<NewArticleCandidate> newArticles(
            @RequestParam(defaultValue = "false") boolean lasttest) {
        return lasttest
                ? articleSyncService.findNewArticlesFromLasttestContingents()
                : articleSyncService.findNewArticlesFromContingents();
    }

    /**
     * POST /api/load/new-articles/create-next
     * Legt den nächsten noch nicht angelegten Artikel aus der Lasttest-Tabelle an.
     * 200 + ArticleInfo  → Artikel erfolgreich angelegt
     * 204 No Content     → keine neuen Artikel mehr vorhanden
     */
    @PostMapping("/new-articles/create-next")
    public ResponseEntity<ArticleInfo> createNextNewArticle() {
        try {
            Optional<ArticleInfo> result = articleSyncService.createNextFromLasttest();
            return result.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.noContent().build());
        } catch (Exception e) {
            // Konkurrente Erstellung – kein Fehler für den Test
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * POST /api/load/new-articles/create-next-with-storage
     * Legt den nächsten Artikel an und weist ihm automatisch einen freien Lagerplatz zu.
     * 200 + ArticleInfo  → Artikel erfolgreich angelegt
     * 204 No Content     → keine neuen Artikel mehr vorhanden
     * 503 Service Unavailable → kein freier Lagerplatz vorhanden
     */
    @PostMapping("/new-articles/create-next-with-storage")
    public ResponseEntity<ArticleInfo> createNextNewArticleWithStorage() {
        List<StorageLocation> available = storageLocationService.findAllAvailable();
        if (available.isEmpty()) {
            // Keine Lagerplaetze verfuegbar – aber nur 503 wenn noch Artikel-Kandidaten existieren.
            // Wenn keine Kandidaten mehr da sind, direkt 204 zurueckgeben damit der JMeter-Thread
            // sofort stoppt statt sinnlos einen neuen Lagerplatz anzulegen.
            if (articleSyncService.countNewArticlesFromLasttest() == 0) {
                return ResponseEntity.noContent().build();
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Keine freien Lagerplatze verfugbar – bitte zuerst Lagerplatze anlegen.");
        }

        // Shuffeln damit parallele Threads nicht alle denselben Lagerplatz treffen
        Collections.shuffle(available);

        for (StorageLocation loc : available) {
            try {
                loc.setStorageStatus("Used");
                storageLocationService.save(loc);
                // Lagerplatz gesichert – jetzt Artikel anlegen
                Optional<ArticleInfo> result = articleSyncService.createNextWithStorageLocation(loc.getGeneralId());
                if (result.isPresent()) {
                    return ResponseEntity.ok(result.get());
                }
                // Keine Kandidaten mehr – Lagerplatz wieder freigeben
                loc.setStorageStatus("Available");
                storageLocationService.save(loc);
                return ResponseEntity.noContent().build();
            } catch (ObjectOptimisticLockingFailureException e) {
                // Anderer Thread hat diesen Lagerplatz gleichzeitig belegt – naechsten probieren
            }
        }

        // Alle verfuegbaren Lagerplaetze wurden von parallelen Requests belegt
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Keine freien Lagerplatze verfugbar – bitte zuerst Lagerplatze anlegen.");
    }

    /** GET /api/load/messaging-events – Messaging-Events */
    @GetMapping("/messaging-events")
    public List<MessagingEvent> messagingEvents() {
        return messagingEventService.findAll();
    }
}
