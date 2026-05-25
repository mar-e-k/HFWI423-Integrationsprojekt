package com.example.application.api.kommission;

import com.example.application.amqp.storeEvents.LogisticEventPublisher;
import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.KommissionRepository;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import com.example.application.services.ArticleInfoService;
import com.example.application.services.ArticleSyncService;
import com.example.application.services.KommissionService;
import com.example.application.services.NewArticleCandidate;
import com.example.application.services.WeeklyKommissionScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/kommissionen")
@Tag(name = "Kommissionen", description = "Kommissionierung und Filial-Bestellungen")
public class KommissionController {

    private final KommissionService kommissionService;
    private final KommissionRepository kommissionRepository;
    private final MessageLogisticRepository messageLogisticRepository;
    private final ArticleInfoService articleInfoService;
    private final ArticleInfoRepository articleInfoRepository;
    private final ArticleSyncService articleSyncService;
    private final WeeklyKommissionScheduler weeklyKommissionScheduler;
    private final LogisticEventPublisher logisticEventPublisher;

    public KommissionController(KommissionService kommissionService,
                                KommissionRepository kommissionRepository,
                                MessageLogisticRepository messageLogisticRepository,
                                ArticleInfoService articleInfoService,
                                ArticleInfoRepository articleInfoRepository,
                                ArticleSyncService articleSyncService,
                                WeeklyKommissionScheduler weeklyKommissionScheduler,
                                LogisticEventPublisher logisticEventPublisher) {
        this.kommissionService = kommissionService;
        this.kommissionRepository = kommissionRepository;
        this.messageLogisticRepository = messageLogisticRepository;
        this.articleInfoService = articleInfoService;
        this.articleInfoRepository = articleInfoRepository;
        this.articleSyncService = articleSyncService;
        this.weeklyKommissionScheduler = weeklyKommissionScheduler;
        this.logisticEventPublisher = logisticEventPublisher;
    }

    record SetQuantityRequest(long quantity) {}

    @Operation(summary = "Alle Kommissionen abrufen")
    @GetMapping
    public List<Kommission> kommissionen() {
        return kommissionService.getAlleKommissionen();
    }

    @Operation(summary = "W\u00f6chentliche Kommissionierung ausl\u00f6sen")
    @PostMapping("/trigger")
    public Map<String, String> triggerWeeklyKommissionen() {
        weeklyKommissionScheduler.createWeeklyKommissionen();
        return Map.of("result", "Woechentliche Kommissionierung ausgeloest");
    }

    @Operation(summary = "Kommission abschlie\u00dfen")
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
            String articleNumber = msg.getArticleNumber();
            if (articleNumber == null && msg.getArticleId() != null) {
                try {
                    articleNumber = articleInfoService.findById(msg.getArticleId()).getArticleNumber();
                } catch (Exception ignored) {}
            }
            if (articleNumber == null) continue;
            articleInfoService.updateStock(articleNumber, (int) msg.getQuantity());
            try {
                long storeIdLong = Long.parseLong(kommission.getStoreId().replaceAll("[^0-9]", ""));
                Long articleId = msg.getArticleId();
                if (articleId != null) {
                    logisticEventPublisher.publishArticleDelivery(storeIdLong, articleId, msg.getQuantity());
                }
            } catch (Exception ignored) {}
        }
        kommission.setFinished(true);
        return kommissionService.save(kommission);
    }

    @Operation(summary = "Simulierte Filial-Bestellungen erstellen")
    @PostMapping("/simulate-store-orders")
    @Transactional
    public ResponseEntity<Map<String, Object>> simulateStoreOrders(
            @RequestParam(defaultValue = "3") int stores) {
        List<ArticleInfo> articlesWithStock = articleInfoRepository.findAll().stream()
                .filter(a -> a.getTotalStock() != null && a.getTotalStock() > 0 && a.getArticleId() != null)
                .toList();
        List<NewArticleCandidate> newCandidates = articleSyncService.findNewArticlesFromLasttestContingents();
        Random random = new Random();
        List<MessageLogistic> batch = new ArrayList<>();
        for (int s = 1; s <= stores; s++) {
            String storeId = "Lasttest-Store-" + s;
            messageLogisticRepository.deleteProcessedByStore(storeId);
            messageLogisticRepository.deleteAllUnprocessedByStore(storeId);
            for (ArticleInfo article : articlesWithStock) {
                MessageLogistic msg = new MessageLogistic();
                msg.setStoreId(storeId);
                msg.setArticleNumber(article.getArticleNumber());
                msg.setArticleId(article.getArticleId());
                msg.setQuantity(1 + random.nextInt(5));
                msg.setProcessed(false);
                batch.add(msg);
            }
            for (NewArticleCandidate candidate : newCandidates) {
                MessageLogistic msg = new MessageLogistic();
                msg.setStoreId(storeId);
                msg.setArticleNumber(candidate.getArticleNumber());
                msg.setArticleId(candidate.getArticleId());
                msg.setQuantity(1 + random.nextInt(5));
                msg.setProcessed(false);
                batch.add(msg);
            }
        }
        messageLogisticRepository.saveAll(batch);
        return ResponseEntity.ok(Map.of("createdOrders", batch.size(), "stores", stores,
                "fromArticleInfo", articlesWithStock.size(), "fromNewArticles", newCandidates.size()));
    }

    @Operation(summary = "Alle offenen Kommissionen abschlie\u00dfen")
    @PostMapping("/finish-all")
    public ResponseEntity<Integer> finishAll() {
        List<Kommission> open = kommissionRepository.findAll().stream()
                .filter(k -> !Boolean.TRUE.equals(k.getFinished()))
                .toList();
        int finished = 0;
        for (Kommission kommission : open) {
            try {
                kommissionService.finishAtomar(kommission);
                finished++;
            } catch (Exception e) {
                System.out.println("[finish-all] FEHLER Kommission " + kommission.getId() + ": " + e.getMessage());
            }
        }
        return ResponseEntity.ok(finished);
    }

    @Operation(summary = "Realisierte Menge einer Kommissionsposition setzen")
    @PutMapping("/{id}/items/{articleId}/quantity")
    public MessageLogistic setItemQuantity(@PathVariable Long id,
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
