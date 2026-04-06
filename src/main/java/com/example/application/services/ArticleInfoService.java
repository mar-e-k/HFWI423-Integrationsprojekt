package com.example.application.services;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.storageLocation.StorageLocation;
import com.vaadin.flow.component.html.Article;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import com.example.application.data.stockChangeLog.StockChangeLogRepository;
import com.vaadin.flow.data.provider.ListDataProvider;
import jakarta.persistence.EntityNotFoundException;
import com.example.application.data.stockChangeLog.StockChangeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.example.application.data.stockChangeLog.ChangeType;
import com.example.application.services.StorageLocationService;
import com.example.application.data.storageLocation.StorageLocation;

@Service
public class ArticleInfoService {

    private final ArticleInfoRepository articleInfoRepository;

    private final StorageLocationService storageLocationService;

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

    public ArticleInfoService(ArticleInfoRepository repository, StorageLocationService storageLocationService,
                              StockChangeLogRepository logRepository) {
        this.articleInfoRepository = repository;
        this.storageLocationService = storageLocationService;
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
        int newStock = oldStock + delta; // "rohes" Ergebnis

        int piecesPerPallet = managed.getPiecesPerPallet() != null ? managed.getPiecesPerPallet() : 0;
        int reservePallets  = managed.getReservePallets()   != null ? managed.getReservePallets()   : 0;

        // -------- Palettenlogik: so viele Paletten öffnen wie nötig --------
        if (piecesPerPallet > 0) {
            // solange Bestand <= 0 UND noch Paletten da sind → Palette(n) öffnen
            while (newStock <= 0 && reservePallets > 0) {
                newStock += piecesPerPallet; // Palette aufreißen
                reservePallets--;            // eine Palette weniger in Reserve
            }
        }

        // Falls trotz aller Paletten negativ → bei 0 stoppen
        if (newStock < 0) {
            newStock = 0;
        }

        managed.setStockLevel(newStock);
        managed.setReservePallets(reservePallets);

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

    //Passt die zu kommissionierende Menge nicht mit der Menge im Stock_Level überein bzw. ist größer, so wird eine neue Palette angebrochen
    @Transactional
    public boolean updateStock(String articleNumber, int change) {

        ArticleInfo article = articleInfoRepository.findByArticleNumber(articleNumber);

        if (article == null) {
            System.out.println("Artikel existiert NICHT in article_info!");
            return false;
        }

        applyStockChange(article, -change, ChangeType.ISSUE, "Kommissionierung", "system");
        System.out.println("erfolgreich geupdated");
        return true;
    }

    public boolean existsForLocation(String generalId) {
        if (generalId == null || generalId.isBlank()) {
            return false;
        }
        return articleInfoRepository.existsByStorageLocation(generalId);
    }

    //das ist für die Kommission wichtig
    public ArticleInfo findById(long id) {
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
        ArticleInfo article = articleInfoRepository.findByArticleId(articleId);

        if (article == null) {
            throw new IllegalArgumentException("Artikel existiert NICHT in article_info!");
        }

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

    //Für spätere Logik
    @Transactional
    public void deleteArticle(Long articleId) {
        ArticleInfo article = articleInfoRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Artikel " + articleId + " nicht gefunden"));

        String generalId = article.getStorageLocation();
        if (generalId != null && !generalId.isBlank()) {
            StorageLocation parsed = parseGeneralIdToLocation(generalId); // Helfermethode unten
            if (parsed != null) {
                storageLocationService
                        .findByZoneShelfCompartment(
                                parsed.getStorageZone(),
                                parsed.getShelfID(),
                                parsed.getCompartmentID()
                        )
                        .ifPresent(loc -> {
                            loc.setStorageStatus("Available");
                            storageLocationService.save(loc);
                        });
            }
        }

        articleInfoRepository.delete(article);
    }

    // Helfer: generalId (z.B. "Z3.S2.C4") zurück in Zone/Shelf/Compartment übersetzen
    private StorageLocation parseGeneralIdToLocation(String generalId) {
        if (generalId == null || generalId.isBlank()) {
            return null;
        }

        // Erwartetes Format: Z3.S2.C4
        try {
            String[] parts = generalId.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            String zonePart = parts[0];  // "Z3"
            String shelfPart = parts[1]; // "S2"
            String compPart = parts[2];  // "C4"

            int zoneNumber = Integer.parseInt(zonePart.substring(1));
            int shelfId = Integer.parseInt(shelfPart.substring(1));
            int compId = Integer.parseInt(compPart.substring(1));

            String zoneString = "Zone " + zoneNumber;

            StorageLocation tmp = new StorageLocation();
            tmp.setStorageZone(zoneString);
            tmp.setShelfID(shelfId);
            tmp.setCompartmentID(compId);
            return tmp;
        } catch (Exception e) {
            return null;
        }
    }
}




