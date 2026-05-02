package com.example.application.services;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WeeklyKommissionScheduler {

    private final KommissionService kommissionService;
    private final MessageLogisticRepository msgRepo;
    private final ArticleInfoRepository articleRepo;

    // C: Self-Referenz damit processStore() ueber den Spring-Proxy laeuft
    // und @Transactional(REQUIRES_NEW) tatsaechlich feuert
    @Autowired
    @Lazy
    private WeeklyKommissionScheduler self;

    public WeeklyKommissionScheduler(KommissionService kommissionService,
                                     MessageLogisticRepository msgRepo,
                                     ArticleInfoRepository articleRepo) {
        this.kommissionService = kommissionService;
        this.msgRepo = msgRepo;
        this.articleRepo = articleRepo;
    }

    // C: Kein @Transactional hier — jeder Store bekommt seine eigene Transaktion via processStore()
    @Scheduled(cron = "0 0 10 * * MON")
    public void createWeeklyKommissionen() {
        List<String> stores = msgRepo.findDistinctStoresWithUnprocessed();
        for (String storeId : stores) {
            self.processStore(storeId);
        }
        System.out.println("Wöchentliche Kommissionen erstellt.");
    }

    // C: Eigene Transaktion pro Store — ein Store-Fehler rollt nur diesen Store zurueck
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processStore(String storeId) {
        // D: SELECT FOR UPDATE — sperrt Rows, parallele Trigger koennen dieselben Messages nicht mehr lesen
        List<MessageLogistic> offeneMessages =
                msgRepo.findByStoreIdAndQuantityGreaterThanAndProcessedFalseForUpdate(storeId, 0);

        if (offeneMessages.isEmpty()) {
            return;
        }

        // A: articleIds fuer Batch-Load sammeln (direkt aus msg.articleId + numeric fallback)
        List<Long> articleIds = offeneMessages.stream()
                .map(msg -> {
                    if (msg.getArticleId() != null) return msg.getArticleId();
                    if (msg.getArticleNumber() != null && !msg.getArticleNumber().isBlank()) {
                        try { return Long.valueOf(msg.getArticleNumber()); } catch (NumberFormatException ignored) {}
                    }
                    return null;
                })
                .filter(id -> id != null)
                .distinct()
                .toList();

        // A: Nicht-numerische articleNumbers fuer Fallback-Batch-Load sammeln
        List<String> fallbackNumbers = offeneMessages.stream()
                .filter(msg -> msg.getArticleId() == null)
                .map(MessageLogistic::getArticleNumber)
                .filter(n -> {
                    if (n == null || n.isBlank()) return false;
                    try { Long.valueOf(n); return false; } catch (NumberFormatException e) { return true; }
                })
                .distinct()
                .toList();

        // A: Einmal Batch-Load statt N × findByArticleId / findByArticleNumber
        Map<Long, ArticleInfo> byArticleId = articleRepo.findAllByArticleIdIn(articleIds).stream()
                .collect(Collectors.toMap(ArticleInfo::getArticleId, a -> a, (a, b) -> a));

        Map<String, ArticleInfo> byArticleNumber = articleRepo.findAllByArticleNumberIn(fallbackNumbers).stream()
                .collect(Collectors.toMap(ArticleInfo::getArticleNumber, a -> a, (a, b) -> a));

        List<MessageLogistic> lieferbareMessages = new ArrayList<>();

        for (MessageLogistic msg : offeneMessages) {
            ArticleInfo article = resolveArticleFromMaps(msg, byArticleId, byArticleNumber);
            if (article == null) continue;
            if (article.getTotalStock() == null || article.getTotalStock() <= 0) continue;
            lieferbareMessages.add(msg);
        }

        if (lieferbareMessages.isEmpty()) {
            return;
        }

        Kommission k = new Kommission();
        k.setStoreId(storeId);
        k.setDate(LocalDateTime.now());
        k.setFinished(false);
        // E+F: nextOrderNumber() ist atomar via COALESCE(MAX(order_picking_nr), 0)+1 auf DB-Ebene
        k.setOrderPickingNumber(kommissionService.generateNextOrderPickingNumber());

        kommissionService.save(k);

        // B: Alle Messages in einer Liste, dann ein saveAll() statt N einzelne save()
        for (MessageLogistic msg : lieferbareMessages) {
            msg.setKommission(k);
            msg.setProcessed(true);
        }
        msgRepo.saveAll(lieferbareMessages);
    }

    // A: In-Memory-Aufloesung ohne DB-Aufruf pro Message
    private ArticleInfo resolveArticleFromMaps(MessageLogistic msg,
                                               Map<Long, ArticleInfo> byArticleId,
                                               Map<String, ArticleInfo> byArticleNumber) {
        if (msg.getArticleId() != null) {
            return byArticleId.get(msg.getArticleId());
        }
        if (msg.getArticleNumber() != null && !msg.getArticleNumber().isBlank()) {
            try {
                return byArticleId.get(Long.valueOf(msg.getArticleNumber()));
            } catch (NumberFormatException e) {
                return byArticleNumber.get(msg.getArticleNumber());
            }
        }
        return null;
    }
}
