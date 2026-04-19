package com.example.application.services;

import com.example.application.amqp.storeEvents.LogisticEventPublisher;
import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        for (MessageLogistic msg : items) {
            String articleNumber = msg.getArticleNumber();
            if (articleNumber == null && msg.getArticleId() != null) {
                try {
                    articleNumber = artikelService.findById(msg.getArticleId()).getArticleNumber();
                } catch (Exception ignored) { }
            }
            if (articleNumber == null) continue;
            artikelService.updateStock(articleNumber, (int) msg.getQuantity());
            // Als verarbeitet markieren damit AMQP-Cleanup (deleteProcessedByStore) greift
            msg.setProcessed(true);
            msgRepo.save(msg);
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