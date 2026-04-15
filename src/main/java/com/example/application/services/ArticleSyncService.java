package com.example.application.services;


import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.contingent.Contingent;
import com.example.application.data.contingent.ContingentLasttest;
import com.example.application.data.contingent.ContingentLasttestRepository;
import com.example.application.data.contingent.ContingentRepository;
import com.example.application.data.externalArticle.ExternalArticle;
import com.example.application.data.externalArticle.ExternalArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
public class ArticleSyncService {

    private final ContingentRepository contingentRepository;
    private final ContingentLasttestRepository contingentLasttestRepository;
    private final ExternalArticleRepository externalArticleRepository;
    private final ArticleInfoRepository articleInfoRepository;

    public ArticleSyncService(ContingentRepository contingentRepository,
                              ContingentLasttestRepository contingentLasttestRepository,
                              ExternalArticleRepository externalArticleRepository,
                              ArticleInfoRepository articleInfoRepository) {
        this.contingentRepository = contingentRepository;
        this.contingentLasttestRepository = contingentLasttestRepository;
        this.externalArticleRepository = externalArticleRepository;
        this.articleInfoRepository = articleInfoRepository;
    }

    @Transactional(readOnly = true)
    public long countNewArticles() {
        return contingentRepository.countNewArticles();
    }

    @Transactional(readOnly = true)
    public long countNewArticlesFromLasttest() {
        return findNewArticlesFromLasttestContingents().size();
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

    @Transactional(readOnly = true)
    public List<NewArticleCandidate> findNewArticlesFromLasttestContingents() {
        List<ContingentLasttest> contingents = contingentLasttestRepository.findAll();
        Set<String> existingArticleNumbers = articleInfoRepository.findAllArticleNumbers();

        // Synthetische neue Artikel (sim_article_number gesetzt) – direkt aus der Lasttest-Tabelle
        Map<String, NewArticleCandidate> result = new LinkedHashMap<>();
        for (ContingentLasttest c : contingents) {
            if (c.isSynthetic()) {
                String nr = c.getSimArticleNumber();
                if (!existingArticleNumbers.contains(nr) && !result.containsKey(nr)) {
                    NewArticleCandidate candidate = new NewArticleCandidate();
                    candidate.setArticleId(c.getArticleId());
                    candidate.setArticleNumber(nr);
                    candidate.setName(c.getSimArticleName());
                    result.put(nr, candidate);
                }
            }
        }

        // Echte neue Artikel (articleId > 0, ExternalArticle vorhanden, noch kein ArticleInfo)
        Set<Long> echteIds = contingents.stream()
                .filter(c -> !c.isSynthetic())
                .map(ContingentLasttest::getArticleId)
                .collect(Collectors.toSet());

        if (!echteIds.isEmpty()) {
            Map<Long, ExternalArticle> externalById = externalArticleRepository.findAllById(echteIds)
                    .stream()
                    .collect(Collectors.toMap(ExternalArticle::getId, Function.identity()));

            for (Long articleId : echteIds) {
                ExternalArticle ext = externalById.get(articleId);
                if (ext == null) continue;
                if (existingArticleNumbers.contains(ext.getArticleNumber())) continue;
                if (result.containsKey(ext.getArticleNumber())) continue;

                NewArticleCandidate candidate = new NewArticleCandidate();
                candidate.setArticleId(articleId);
                candidate.setArticleNumber(ext.getArticleNumber());
                candidate.setName(ext.getName());
                result.put(ext.getArticleNumber(), candidate);
            }
        }

        List<NewArticleCandidate> sorted = new ArrayList<>(result.values());
        sorted.sort(Comparator.comparing(NewArticleCandidate::getArticleNumber));
        return sorted;
    }

    /**
     * Erstellt den nächsten noch nicht angelegten Artikel aus der Lasttest-Tabelle.
     * Konkurrenz-sicher: bei Duplikat wird einfach der nächste Kandidat versucht.
     * Gibt Optional.empty() zurück wenn keine neuen Artikel mehr vorhanden.
     */
    @Transactional
    public Optional<ArticleInfo> createNextFromLasttest() {
        List<NewArticleCandidate> candidates = findNewArticlesFromLasttestContingents();
        for (NewArticleCandidate candidate : candidates) {
            if (articleInfoRepository.findByArticleNumber(candidate.getArticleNumber()) != null) {
                continue; // anderer Thread war schneller
            }
            try {
                ArticleInfo info = new ArticleInfo();
                info.setArticleNumber(candidate.getArticleNumber());
                info.setName(candidate.getName());
                if (candidate.getArticleId() != null && candidate.getArticleId() > 0) {
                    info.setArticleId(candidate.getArticleId());
                }
                info.setStockLevel(0);
                info.setStorageLocation("UNGESETZT");
                info.setReservePallets(0);
                info.setMinStock(5);
                info.setPiecesPerPallet(100);
                return Optional.of(articleInfoRepository.save(info));
            } catch (Exception e) {
                // Konkurrenter Zugriff – nächsten Kandidaten probieren
            }
        }
        return Optional.empty();
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
