package com.example.application.services;


import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.contingent.Contingent;
import com.example.application.data.contingent.ContingentRepository;
import com.example.application.data.externalArticle.ExternalArticle;
import com.example.application.data.externalArticle.ExternalArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ArticleSyncService {

    private final ContingentRepository contingentRepository;
    private final ExternalArticleRepository externalArticleRepository;
    private final ArticleInfoRepository articleInfoRepository;

    public ArticleSyncService(ContingentRepository contingentRepository,
                              ExternalArticleRepository externalArticleRepository,
                              ArticleInfoRepository articleInfoRepository) {
        this.contingentRepository = contingentRepository;
        this.externalArticleRepository = externalArticleRepository;
        this.articleInfoRepository = articleInfoRepository;
    }

    @Transactional(readOnly = true)
    public List<NewArticleCandidate> findNewArticlesFromContingents() {
        List<Contingent> contingents = contingentRepository.findAll();

        // pro articleId nur einmal
        Map<Long, Contingent> byArticleId = contingents.stream()
                .collect(Collectors.toMap(
                        Contingent::getArticleId,
                        Function.identity(),
                        (a, b) -> a // Duplikate ignorieren
                ));

        List<NewArticleCandidate> result = new ArrayList<>();

        for (Long articleId : byArticleId.keySet()) {
            ExternalArticle ext = externalArticleRepository.findById(articleId).orElse(null);
            if (ext == null) {
                continue;
            }

            // wenn es schon ArticleInfo dazu gibt -> nicht mehr "neu"
            if (articleInfoRepository.findByArticleNumber(ext.getArticleNumber()) != null) {
                continue;
            }

            NewArticleCandidate c = new NewArticleCandidate();
            c.setArticleId(articleId);
            c.setArticleNumber(ext.getArticleNumber());
            c.setName(ext.getName());
            result.add(c);
        }

        //sortieren
        result.sort(Comparator.comparing(NewArticleCandidate::getArticleNumber));
        return result;
    }

    /**
     * Legt einen ArticleInfo für einen Artikel an.
     */
    @Transactional
    public ArticleInfo createArticleInfoForCandidate(Long articleId,
                                                     String storageLocation,
                                                     Integer piecesPerPallet,
                                                     Integer minStock) {

        if (storageLocation == null || storageLocation.isBlank()) {
            throw new IllegalArgumentException("Storage Location ist erforderlich.");
        }
        if (piecesPerPallet == null || piecesPerPallet <= 0) {
            throw new IllegalArgumentException("Pieces per Pallet muss > 0 sein.");
        }
        if (minStock == null || minStock < 0) {
            throw new IllegalArgumentException("Mindestbestand darf nicht negativ sein.");
        }

        ExternalArticle ext = externalArticleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Artikel " + articleId + " nicht gefunden."));

        if (articleInfoRepository.findByArticleNumber(ext.getArticleNumber()) != null) {
            throw new IllegalStateException("ArticleInfo für " + ext.getArticleNumber() + " existiert bereits.");
        }

        ArticleInfo info = new ArticleInfo();
        info.setArticleNumber(ext.getArticleNumber());
        info.setName(ext.getName());

        info.setStorageLocation(storageLocation);
        info.setPiecesPerPallet(piecesPerPallet);
        info.setMinStock(minStock);      // ⬅️ hier wird der Mindestbestand gesetzt

        // Defaults
        info.setStockLevel(0);
        info.setReservePallets(0);
        info.setReserveStorageLocation(null);

        return articleInfoRepository.save(info);
    }

    @Transactional
    public ArticleInfo createArticleInfoForSingleContingentArticle(Long articleId) {

        if (articleId == null) {
            throw new IllegalArgumentException("articleId darf nicht null sein");
        }

        // Artikel aus externer Artikel-Tabelle holen
        ExternalArticle ext = externalArticleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Artikel mit ID " + articleId + " nicht gefunden."));

        // Prüfen, ob es schon einen ArticleInfo dafür gibt
        ArticleInfo existing = articleInfoRepository.findByArticleNumber(ext.getArticleNumber());
        if (existing != null) {
            // schon vorhanden → egal
            return existing;
        }

        // Neuen ArticleInfo mit Basisdaten anlegen
        ArticleInfo info = new ArticleInfo();
        info.setArticleNumber(ext.getArticleNumber());
        info.setName(ext.getName());


        info.setStockLevel(0);
        info.setStorageLocation("UNGESETZT");
        info.setReserveStorageLocation(null);
        info.setMinStock(null);
        info.setPiecesPerPallet(null);
        info.setReservePallets(0);

        return articleInfoRepository.save(info);
    }
}
