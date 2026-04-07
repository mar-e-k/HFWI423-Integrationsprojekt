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
    public long countNewArticles() {
        return contingentRepository.countNewArticles();
    }

    @Transactional(readOnly = true)
    public List<NewArticleCandidate> findNewArticlesFromContingents() {
        // Query 1: alle Contingents
        List<Contingent> contingents = contingentRepository.findAll();

        // pro articleId nur einmal
        Set<Long> uniqueArticleIds = contingents.stream()
                .map(Contingent::getArticleId)
                .collect(Collectors.toSet());

        // Query 2: alle ExternalArticles auf einmal (statt N einzelner findById-Calls)
        Map<Long, ExternalArticle> externalById = externalArticleRepository.findAllById(uniqueArticleIds)
                .stream()
                .collect(Collectors.toMap(ExternalArticle::getId, Function.identity()));

        // Query 3: alle bereits angelegten ArticleNumbers auf einmal
        Set<String> existingArticleNumbers = articleInfoRepository.findAllArticleNumbers();

        List<NewArticleCandidate> result = new ArrayList<>();

        for (Long articleId : uniqueArticleIds) {
            ExternalArticle ext = externalById.get(articleId);
            if (ext == null) {
                continue;
            }

            // wenn es schon ArticleInfo dazu gibt -> nicht mehr "neu"
            if (existingArticleNumbers.contains(ext.getArticleNumber())) {
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
        info.setArticleId(articleId);
        info.setArticleNumber(ext.getArticleNumber());
        info.setName(ext.getName());
        info.setStorageLocation(storageLocation);
        info.setPiecesPerPallet(piecesPerPallet);
        info.setMinStock(minStock);

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
