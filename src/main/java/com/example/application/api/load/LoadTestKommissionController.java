package com.example.application.api.load;

import com.example.application.amqp.storeEvents.LogisticEventPublisher;
import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.KommissionRepository;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import com.example.application.services.ArticleInfoService;
import com.example.application.services.KommissionService;
import com.example.application.services.WeeklyKommissionScheduler;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Lasttest-Endpunkte fuer OrderPickingView.
 * Basis-URL: /api/load/kommissionen
 */
@RestController
@RequestMapping("/api/load/kommissionen")
public class LoadTestKommissionController {

    private final KommissionService kommissionService;
    private final KommissionRepository kommissionRepository;
    private final MessageLogisticRepository messageLogisticRepository;
    private final ArticleInfoService articleInfoService;
    private final WeeklyKommissionScheduler weeklyKommissionScheduler;
    private final LogisticEventPublisher logisticEventPublisher;

    public LoadTestKommissionController(KommissionService kommissionService,
                                         KommissionRepository kommissionRepository,
                                         MessageLogisticRepository messageLogisticRepository,
                                         ArticleInfoService articleInfoService,
                                         WeeklyKommissionScheduler weeklyKommissionScheduler,
                                         LogisticEventPublisher logisticEventPublisher) {
        this.kommissionService = kommissionService;
        this.kommissionRepository = kommissionRepository;
        this.messageLogisticRepository = messageLogisticRepository;
        this.articleInfoService = articleInfoService;
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

    /** PUT /api/load/kommissionen/{id}/finish – Kommission abschliessen */
    @PutMapping("/{id}/finish")
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
            articleInfoService.updateStock(msg.getArticleNumber(), (int) msg.getQuantity());
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
