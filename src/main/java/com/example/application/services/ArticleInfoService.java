package com.example.application.services;

import com.example.application.data.article.ArticleInfo;
import com.example.application.data.article.ArticleInfoRepository;

import java.util.List;
import java.util.Optional;

import com.example.application.data.stockChangeLog.StockChangeLogRepository;
import com.vaadin.flow.data.provider.ListDataProvider;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import com.example.application.data.stockChangeLog.StockChangeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.example.application.data.stockChangeLog.ChangeType;

@Service
public class ArticleInfoService {

    private final ArticleInfoRepository articleInfoRepository;

    public Optional<ArticleInfo> get(Long id) {
        return articleInfoRepository.findById(id);
    }

    public ArticleInfo save(ArticleInfo entity) {
        return articleInfoRepository.save(entity);
    }

    public void delete(Long id) {
        articleInfoRepository.deleteById(id);
    }

    public Page<ArticleInfo> list(Pageable pageable) {
        return articleInfoRepository.findAll(pageable);
    }

    public Page<ArticleInfo> list(Pageable pageable, Specification<ArticleInfo> filter) {
        return articleInfoRepository.findAll(filter, pageable);
    }

    public int count() {
        return (int) articleInfoRepository.count();
    }

    public long count(Specification<ArticleInfo> filter) {
        return articleInfoRepository.count(filter);
    }

    public ArticleInfoService(ArticleInfoRepository repository,
                              StockChangeLogRepository logRepository) {
        this.articleInfoRepository = repository;
        this.logRepository = logRepository;
    }

    private final StockChangeLogRepository logRepository;

    @Transactional
    public ArticleInfo applyStockChange(ArticleInfo article,
                                        int delta,
                                        ChangeType type,
                                        String reason,
                                        String changedBy) {

        ArticleInfo managed = articleInfoRepository.findById(article.getId())
                .orElseThrow(() -> new IllegalArgumentException("Article not found: " + article.getId()));

        int oldStock = managed.getStockLevel() == null ? 0 : managed.getStockLevel();
        int newStock = oldStock + delta;

        // neuen Bestand
        managed.setStockLevel(newStock);

        // -------- PALLETTEN-LOGIK --------
        int piecesPerPallet = managed.getPiecesPerPallet() != null ? managed.getPiecesPerPallet() : 0;
        int reservePallets = managed.getReservePallets() != null ? managed.getReservePallets() : 0;

        int openStock = managed.getStockLevel() != null ? managed.getStockLevel() : 0;

        // nur sinnvoll, wenn wir wissen wie viele Stück pro Palette
        if (piecesPerPallet > 0) {
            // solange Bestand <= 0 und noch Paletten da sind → Palette(n) öffnen
            while (openStock <= 0 && reservePallets > 0) {
                openStock += piecesPerPallet;  // Palette aufreißen
                reservePallets--;              // eine weniger in Reserve
            }
        }

        // Falls keine Paletten mehr da sind und wir ins Minus gerutscht sind, auf 0 begrenzen
        if (openStock < 0) {
            openStock = 0;
        }

        managed.setStockLevel(openStock);
        managed.setReservePallets(reservePallets);
        // -------- ENDE PALLETTEN-LOGIK --------

        ArticleInfo saved = articleInfoRepository.save(managed);

        StockChangeLog log = new StockChangeLog();
        log.setArticleId(saved.getId());
        log.setArticleNumber(saved.getArticleNumber());
        log.setArticleName(saved.getName());
        log.setOldStock(oldStock);
        log.setDelta(delta);
        log.setNewStock(saved.getStockLevel()); // finaler Bestand nach Palettenlogik
        log.setChangeType(type);
        log.setReason(reason);
        log.setChangedBy(changedBy);
        logRepository.save(log);

        return saved;
    }

    public ListDataProvider<String> findAllStorageLocations() {
        // Holt Lagerorte aus dem Repository
        List<String> locations = articleInfoRepository.findDistinctStorageLocations();

        // Aufräumen & sortieren
        locations.removeIf(s -> s == null || s.isBlank());
        locations.sort(String::compareToIgnoreCase);

        return new ListDataProvider<>(locations);
    }

    public ArticleInfo updateStorageLocation(Long id, String newLocation) {
        ArticleInfo db = articleInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found: " + id));
        db.setStorageLocation(newLocation);
        return articleInfoRepository.save(db);
    }

    @Transactional
    public int updateStorageLocationForAll(String oldLocation, String newLocation) {
        return articleInfoRepository.bulkUpdateStorageLocation(oldLocation, newLocation);
    }

    public boolean existsForLocation(String generalId) {
        if (generalId == null || generalId.isBlank()) {
            return false;
        }
        return articleInfoRepository.existsByStorageLocation(generalId);
    }

    //das ist für die Kommission wichtig
    public ArticleInfo findById(Long id) {
        return articleInfoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artikel nicht gefunden: " + id));
    }

    // Beispiel-Helper zum Reduzieren des Bestands (du hast sowas schon angedeutet)
    @Transactional
    public void reduceStock(ArticleInfo artikel, int amount) {
        artikel.setStockLevel(artikel.getStockLevel() - amount);
        articleInfoRepository.save(artikel);
    }

    /**
     * Setzt den Stück-Bestand neu und wendet die Palettenlogik an:
     * Wenn der Bestand auf <= 0 fällt und Reservepaletten vorhanden sind,
     * wird automatisch eine Palette geöffnet.
     */
    @Transactional
    public ArticleInfo updateStockLevelWithPalletLogic(Long articleId, int newStockLevel) {
        ArticleInfo article = articleInfoRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found: " + articleId));

        // neuen Bestand setzen (aus dem Dialog kommt typischerweise ein absoluter Wert)
        article.setStockLevel(newStockLevel);

        Integer piecesPerPallet = article.getPiecesPerPallet();
        Integer reservePallets = article.getReservePallets();

        // Null-Sicherheit
        if (piecesPerPallet == null) {
            piecesPerPallet = 0;
        }
        if (reservePallets == null) {
            reservePallets = 0;
        }

        // Logik: Wenn Bestand leer/unter 0 UND es gibt Reservepaletten → neue Palette öffnen
        if (article.getStockLevel() == null || article.getStockLevel() <= 0) {
            if (reservePallets > 0 && piecesPerPallet > 0) {
                reservePallets--;                       // eine Palette aus Reserve weg
                article.setReservePallets(reservePallets);
                article.setStockLevel(piecesPerPallet); // Fach wieder voll mit Stückzahl pro Palette
            } else {
                // keine Reserve vorhanden -> Bestand bleibt 0
                article.setStockLevel(0);
            }
        }

        return articleInfoRepository.save(article);
    }
}



