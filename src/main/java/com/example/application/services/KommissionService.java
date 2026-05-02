package com.example.application.services;

import com.example.application.amqp.storeEvents.LogisticEventPublisher;
import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KommissionService {

    @Autowired
    private KommissionRepository komRepo;

    @Autowired
    private KommissionPositionRepository posRepo;

    @Autowired
    private ArticleInfoService artikelService;

    @Autowired
    private MessageLogisticRepository msgRepo;

    @Autowired
    private ArticleInfoRepository articleRepo;

    @Autowired
    private LogisticEventPublisher logisticEventPublisher;

    // Zentrale Auflösung: zuerst über articleId, dann Fallback über articleNumber
    private ArticleInfo resolveArticle(Long articleId, String articleNumber) {
        if (articleId != null) {
            ArticleInfo article = articleRepo.findByArticleId(articleId);
            if (article != null) {
                return article;
            }
        }

        if (articleNumber != null && !articleNumber.isBlank()) {
            return articleRepo.findByArticleNumber(articleNumber);
        }

        return null;
    }

    // Artikelnamen bevorzugt über articleId holen, sonst über articleNumber
    public String getArticleName(Long articleId, String articleNumber) {
        ArticleInfo article = resolveArticle(articleId, articleNumber);

        if (article == null) {
            return "Unbekannter Artikel";
        }

        return article.getName();
    }

    // Lagerplatz bevorzugt über articleId holen, sonst über articleNumber
    public String getStorageLocationForArticle(Long articleId, String articleNumber) {
        ArticleInfo article = resolveArticle(articleId, articleNumber);

        if (article == null) {
            return "Not found";
        }

        return article.getStorageLocation();
    }

    // Gesamtbestand aus offenem Bestand + Reservepaletten berechnen
    public int getFullStockLevelForArticle(Long articleId, String articleNumber) {
        ArticleInfo article = resolveArticle(articleId, articleNumber);

        if (article == null) {
            return 0;
        }

        int stockLevel = article.getStockLevel() != null ? article.getStockLevel() : 0;
        int piecesPerPallet = article.getPiecesPerPallet() != null ? article.getPiecesPerPallet() : 0;
        int reservePallets = article.getReservePallets() != null ? article.getReservePallets() : 0;

        return stockLevel + piecesPerPallet * reservePallets;
    }

    // Nur offenen Fachbestand holen
    public int getStockLevelForArticle(Long articleId, String articleNumber) {
        ArticleInfo article = resolveArticle(articleId, articleNumber);

        if (article == null) {
            return 0;
        }

        return article.getStockLevel() != null ? article.getStockLevel() : 0;
    }

    public List<Kommission> getOffeneKommissionen() {
        return komRepo.findByFinishedFalseOrderByDateAsc();
    }

    public List<Kommission> getAlleKommissionen() {
        return komRepo.findAllByOrderByDateAsc();
    }

    public Kommission save(Kommission k) {
        return komRepo.save(k);
    }

    /**
     * Verarbeitet eine einzelne Kommission atomar in einer eigenen Transaktion.
     * REQUIRES_NEW: schlaegt diese Kommission fehl, rollt nur sie zurueck –
     * finishAll() kann mit der naechsten weitermachen.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finishAtomar(Kommission kommission) {
        // PESSIMISTIC_WRITE: sperrt die Zeilen -> AMQP-Listener kann sie erst nach Commit loeschen
        List<MessageLogistic> items = msgRepo.findByKommissionIdForUpdate(kommission.getId());

        // Alle articleIds sammeln -> eine Batch-Abfrage statt N findById-Aufrufe
        List<Long> articleIds = items.stream()
                .map(MessageLogistic::getArticleId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, ArticleInfo> articleById = articleRepo.findAllById(articleIds).stream()
                .collect(Collectors.toMap(ArticleInfo::getId, a -> a));

        // storeIdLong einmal berechnen statt in jedem Loop-Durchlauf
        long storeIdLong = -1;
        try {
            storeIdLong = Long.parseLong(kommission.getStoreId().replaceAll("[^0-9]", ""));
        } catch (Exception ignored) {}

        // Stock-Updates in Memory berechnen
        Map<Long, ArticleInfo> articlesToSave = new LinkedHashMap<>();

        for (MessageLogistic msg : items) {
            // ArticleNumber bestimmen: direkt aus msg oder aus batch-geladenem Artikel
            String articleNumber = msg.getArticleNumber();
            ArticleInfo article = msg.getArticleId() != null ? articleById.get(msg.getArticleId()) : null;

            if (articleNumber == null && article != null) {
                articleNumber = article.getArticleNumber();
            }
            if (articleNumber == null) continue;

            // Fallback: nur wenn articleId nicht gesetzt war (Ausnahmefall)
            if (article == null) {
                article = articleRepo.findByArticleNumber(articleNumber);
            }
            if (article == null) continue;

            // Pallet-Logik in Memory berechnen (identisch mit applyStockChange)
            int change = (int) msg.getQuantity();
            int oldStock = article.getStockLevel() != null ? article.getStockLevel() : 0;
            int newStock = oldStock - change;
            int piecesPerPallet = article.getPiecesPerPallet() != null ? article.getPiecesPerPallet() : 0;
            int reservePallets = article.getReservePallets() != null ? article.getReservePallets() : 0;

            if (piecesPerPallet > 0) {
                while (newStock <= 0 && reservePallets > 0) {
                    newStock += piecesPerPallet;
                    reservePallets--;
                }
            }
            if (newStock < 0) newStock = 0;

            article.setStockLevel(newStock);
            article.setReservePallets(reservePallets);
            articlesToSave.put(article.getId(), article);

            // Als verarbeitet markieren damit AMQP-Cleanup (deleteProcessedByStore) greift
            msg.setProcessed(true);

            // Event publishen (kein DB-Query)
            try {
                if (storeIdLong >= 0 && msg.getArticleId() != null) {
                    logisticEventPublisher.publishArticleDelivery(storeIdLong, msg.getArticleId(), msg.getQuantity());
                }
            } catch (Exception ignored) {
                // Event-Publishing schlaegt fehl wenn AMQP nicht verfuegbar
            }
        }

        // Batch-Saves statt N einzelne Saves
        articleRepo.saveAll(articlesToSave.values());
        msgRepo.saveAll(items);

        kommission.setFinished(true);
        komRepo.save(kommission);
    }

    // Prüft bevorzugt über articleId, sonst über articleNumber
    public boolean articleExists(Long articleId, String articleNumber) {
        return resolveArticle(articleId, articleNumber) != null;
    }

    public int generateNextOrderPickingNumber() {
        Integer last = komRepo.findMaxOrderNumber();
        return (last == null ? 1 : last + 1);
    }
}