package com.example.application.api.load;

import com.example.application.data.articleInfo.RestockItem;
import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.data.stockChangeLog.StockChangeLog;
import com.example.application.services.ArticleSyncService;
import com.example.application.services.MessagingEventService;
import com.example.application.services.NewArticleCandidate;
import com.example.application.services.RestockService;
import com.example.application.services.StockChangeLogService;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lasttest-Endpunkte für Views ohne eigene Kategorie:
 * RestockView, StockChangeLogView, NewArticlesView, MessagingView.
 * Basis-URL: /api/load
 */
@RestController
@RequestMapping("/api/load")
public class LoadTestMiscController {

    private final RestockService restockService;
    private final StockChangeLogService stockChangeLogService;
    private final ArticleSyncService articleSyncService;
    private final MessagingEventService messagingEventService;

    public LoadTestMiscController(RestockService restockService,
                                   StockChangeLogService stockChangeLogService,
                                   ArticleSyncService articleSyncService,
                                   MessagingEventService messagingEventService) {
        this.restockService = restockService;
        this.stockChangeLogService = stockChangeLogService;
        this.articleSyncService = articleSyncService;
        this.messagingEventService = messagingEventService;
    }

    /** GET /api/load/restock – Artikel unter Mindestbestand */
    @GetMapping("/restock")
    public List<RestockItem> restock() {
        return restockService.getArticlesToRestock();
    }

    /** GET /api/load/stock-changes – Lagerbestand-Aenderungshistorie */
    @GetMapping("/stock-changes")
    public List<StockChangeLog> stockChanges() {
        return stockChangeLogService.findAll(Sort.by(Sort.Direction.DESC, "changedAt"));
    }

    /** GET /api/load/new-articles – neue Artikel aus Kontingenten */
    @GetMapping("/new-articles")
    public List<NewArticleCandidate> newArticles() {
        return articleSyncService.findNewArticlesFromContingents();
    }

    /** GET /api/load/messaging-events – Messaging-Events */
    @GetMapping("/messaging-events")
    public List<MessagingEvent> messagingEvents() {
        return messagingEventService.findAll();
    }
}
