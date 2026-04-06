package com.example.application.services;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    // Prüft bevorzugt über articleId, sonst über articleNumber
    public boolean articleExists(Long articleId, String articleNumber) {
        return resolveArticle(articleId, articleNumber) != null;
    }

    public int generateNextOrderPickingNumber() {
        Integer last = komRepo.findMaxOrderNumber();
        return (last == null ? 1 : last + 1);
    }
}