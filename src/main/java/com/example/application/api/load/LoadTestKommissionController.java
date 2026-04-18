package com.example.application.api.load;

import com.example.application.amqp.storeEvents.LogisticEventPublisher;
import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.KommissionRepository;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import com.example.application.services.ArticleInfoService;
import com.example.application.services.KommissionService;
import com.example.application.services.WeeklyKommissionScheduler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Lasttest-Endpunkte für OrderPickingView.
 * Basis-URL: /api/load/kommissionen
 */
@RestController
@RequestMapping("/api/load/kommissionen")
public class LoadTestKommissionController {

    private final KommissionService kommissionService;
    private final KommissionRepository kommissionRepository;
    private final MessageLogisticRepository messageLogisticRepository;
    private final ArticleInfoService articleInfoService;
    private final ArticleInfoRepository articleInfoRepository;
    private final WeeklyKommissionScheduler weeklyKommissionScheduler;
    private final LogisticEventPublisher logisticEventPublisher;

    public LoadTestKommissionController(KommissionService kommissionService,
                                         KommissionRepository kommissionRepository,
                                         MessageLogisticRepository messageLogisticRepository,
                                         ArticleInfoService articleInfoService,
                                         ArticleInfoRepository articleInfoRepository,
                                         WeeklyKommissionScheduler weeklyKommissionScheduler,
                                         LogisticEventPublisher logisticEventPublisher) {
        this.kommissionService = kommissionService;
        this.kommissionRepository = kommissionRepository;
        this.messageLogisticRepository = messageLogisticRepository;
        this.articleInfoService = articleInfoService;
        this.articleInfoRepository = articleInfoRepository;
        this.weeklyKommissionScheduler = weeklyKommissionScheduler;
        this.logisticEventPublisher = logisticEventPublisher;
    }

    record SetQuantityRequest(long quantity) {}

    /** GET /api/load/kommissionen – alle Kommissionen */
    @GetMapping
    public List<Kommission> kommissionen() {
        return kommissionService.getAlleKommissionen();
    }

    /** POST /api/load/kommissionen/trigger – woechentliche Kommissionierung ausloesen */
    @PostMapping("/trigger")
    public Map<String, String> triggerWeeklyKommissionen() {
        weeklyKommissionScheduler.createWeeklyKommissionen();
        return Map.of("result", "Woechentliche Kommissionierung ausgeloest");
    }

    /** PUT /api/load/kommissionen/{id}/finish – Kommission abschließen */
    @PutMapping("/{id}/finish")
    @Transactional
    public Kommission finishKommission(@PathVariable Long id) {
        Kommission kommission = kommissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Kommission " + id + " nicht gefunden"));

        if (Boolean.TRUE.equals(kommission.getFinished())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Kommission " + id + " ist bereits abgeschlossen");
        }

        List<MessageLogistic> items = messageLogisticRepository.findByKommissionId(id);
        for (MessageLogistic msg : items) {
            // articleId bevorzugen, articleNumber als Fallback
            String articleNumber = msg.getArticleNumber();
            if (articleNumber == null && msg.getArticleId() != null) {
                try {
                    articleNumber = articleInfoService.findById(msg.getArticleId()).getArticleNumber();
                } catch (Exception ignored) { }
            }
            if (articleNumber == null) continue;

            articleInfoService.updateStock(articleNumber, (int) msg.getQuantity());
            try {
                long storeIdLong = Long.parseLong(kommission.getStoreId().replaceAll("[^0-9]", ""));
                Long articleId   = msg.getArticleId();
                if (articleId != null) {
                    logisticEventPublisher.publishArticleDelivery(storeIdLong, articleId, msg.getQuantity());
                }
            } catch (Exception ignored) {
                // Event-Publishing schlaegt fehl wenn AMQP nicht verfuegbar
            }
        }

        kommission.setFinished(true);
        return kommissionService.save(kommission);
    }

    /**
     * POST /api/load/kommissionen/simulate-store-orders?stores=3
     * Erstellt simulierte Filial-Bestellungen (MessageLogistic) fuer alle Artikel mit Bestand.
     * Jede Bestellung kommt von "Lasttest-Store-1" bis "Lasttest-Store-{stores}".
     * 200 + {"createdOrders": N, "stores": N}
     */
    @PostMapping("/simulate-store-orders")
    @Transactional
    public ResponseEntity<Map<String, Object>> simulateStoreOrders(
            @RequestParam(defaultValue = "3") int stores) {

        List<ArticleInfo> articlesWithStock = articleInfoRepository.findAll().stream()
                .filter(a -> a.getTotalStock() != null && a.getTotalStock() > 0 && a.getArticleId() != null)
                .toList();

        int created = 0;
        Random random = new Random();

        for (int s = 1; s <= stores; s++) {
            String storeId = "Lasttest-Store-" + s;
            messageLogisticRepository.deleteProcessedByStore(storeId);

            for (ArticleInfo article : articlesWithStock) {
                messageLogisticRepository.deleteUnprocessedByStoreAndArticleId(storeId, article.getArticleId());

                MessageLogistic msg = new MessageLogistic();
                msg.setStoreId(storeId);
                msg.setArticleNumber(article.getArticleNumber());
                msg.setArticleId(article.getArticleId());
                msg.setQuantity(1 + random.nextInt(5));
                msg.setProcessed(false);
                messageLogisticRepository.save(msg);
                created++;
            }
        }

        System.out.println("[simulate-store-orders] stores=" + stores + " created=" + created);
        return ResponseEntity.ok(Map.of("createdOrders", created, "stores", stores));
    }

    /**
     * POST /api/load/kommissionen/finish-all
     * Schliesst alle offenen Kommissionen ab (analog zu approve-all).
     * 200 + Anzahl abgeschlossener Kommissionen
     */
    @PostMapping("/finish-all")
    @Transactional
    public ResponseEntity<Integer> finishAll() {
        List<Kommission> open = kommissionRepository.findAll().stream()
                .filter(k -> !Boolean.TRUE.equals(k.getFinished()))
                .toList();

        int finished = 0;
        for (Kommission kommission : open) {
            try {
                List<MessageLogistic> items = messageLogisticRepository.findByKommissionId(kommission.getId());
                for (MessageLogistic msg : items) {
                    String articleNumber = msg.getArticleNumber();
                    if (articleNumber == null && msg.getArticleId() != null) {
                        try {
                            articleNumber = articleInfoService.findById(msg.getArticleId()).getArticleNumber();
                        } catch (Exception ignored) { }
                    }
                    if (articleNumber == null) continue;
                    articleInfoService.updateStock(articleNumber, (int) msg.getQuantity());
                    try {
                        long storeIdLong = Long.parseLong(kommission.getStoreId().replaceAll("[^0-9]", ""));
                        Long articleId = msg.getArticleId();
                        if (articleId != null) {
                            logisticEventPublisher.publishArticleDelivery(storeIdLong, articleId, msg.getQuantity());
                        }
                    } catch (Exception ignored) {
                        // Event-Publishing schlaegt fehl wenn AMQP nicht verfuegbar
                    }
                }
                kommission.setFinished(true);
                kommissionService.save(kommission);
                finished++;
            } catch (Exception e) {
                System.out.println("[finish-all] FEHLER Kommission " + kommission.getId() + ": " + e.getMessage());
            }
        }

        System.out.println("[finish-all] finished=" + finished);
        return ResponseEntity.ok(finished);
    }

    /** PUT /api/load/kommissionen/{id}/items/{articleId}/quantity – realisierte Menge setzen */
    @PutMapping("/{id}/items/{articleId}/quantity")
    public MessageLogistic setItemQuantity(
            @PathVariable Long id,
            @PathVariable Long articleId,
            @RequestBody SetQuantityRequest req) {

        List<MessageLogistic> items = messageLogisticRepository.findByKommissionId(id);
        MessageLogistic msg = items.stream()
                .filter(m -> articleId.equals(m.getArticleId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Kein Item mit articleId=" + articleId + " in Kommission " + id));

        msg.setQuantity(req.quantity());
        return messageLogisticRepository.save(msg);
    }
}
