package com.example.application.api.load;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.contingent.ContingentRepository;
import com.example.application.data.stockChangeLog.ChangeType;
import com.example.application.services.ArticleInfoService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * Lasttest-Endpunkte für LogisticMainView.
 * Basis-URL: /api/load/articles
 */
@RestController
@RequestMapping("/api/load/articles")
public class LoadTestArticleController {

    private final ArticleInfoService articleInfoService;
    private final ArticleInfoRepository articleInfoRepository;
    private final ContingentRepository contingentRepository;

    public LoadTestArticleController(ArticleInfoService articleInfoService,
                                     ArticleInfoRepository articleInfoRepository,
                                     ContingentRepository contingentRepository) {
        this.articleInfoService = articleInfoService;
        this.articleInfoRepository = articleInfoRepository;
        this.contingentRepository = contingentRepository;
    }

    record StockChangeRequest(int delta, String reason) {}
    record StorageLocationUpdateRequest(String storageLocation) {}

    /** GET /api/load/articles?page=0&size=20 – Artikelliste paginiert */
    @GetMapping
    public Page<ArticleInfo> articles(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return articleInfoService.list(PageRequest.of(page, size), null);
    }

    /** GET /api/load/articles/filter – Artikel mit Filterparametern */
    @GetMapping("/filter")
    public Page<ArticleInfo> articlesFiltered(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String articleNumber,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) String storageLocation) {

        Specification<ArticleInfo> spec = buildFilterSpec(name, articleNumber, minStock, storageLocation);
        return articleInfoService.list(PageRequest.of(page, size), spec);
    }

    /** POST /api/load/articles/{id}/stock – Bestand ändern */
    @PostMapping("/{id}/stock")
    public ArticleInfo changeStock(
            @PathVariable Long id,
            @RequestBody StockChangeRequest req) {
        try {
            ArticleInfo article = articleInfoService.findById(id);
            String reason = req.reason() != null ? req.reason() : "Lasttest";
            return articleInfoService.applyStockChange(article, req.delta(), ChangeType.ADJUSTMENT, reason, "loadtest");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** PUT /api/load/articles/{id}/storage-location – Lagerplatz zuweisen */
    @PutMapping("/{id}/storage-location")
    public ArticleInfo updateStorageLocation(
            @PathVariable Long id,
            @RequestBody StorageLocationUpdateRequest req) {
        try {
            return articleInfoService.updateStorageLocation(id, req.storageLocation());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** DELETE /api/load/articles/sim – alle SIM-Artikel und ihre Kontingente loeschen */
    @DeleteMapping("/sim")
    @Transactional
    public java.util.Map<String, Object> deleteSimArticles() {
        List<Long> simIds = articleInfoRepository.findAll().stream()
                .filter(a -> a.getArticleNumber() != null && a.getArticleNumber().startsWith("SIM-"))
                .map(ArticleInfo::getId)
                .toList();
        if (!simIds.isEmpty()) {
            contingentRepository.deleteByArticleIdIn(simIds);
        }
        int deleted = articleInfoRepository.deleteAllSimArticles();
        return java.util.Map.of("deleted", deleted);
    }

    private Specification<ArticleInfo> buildFilterSpec(String name, String articleNumber,
                                                        Integer minStock, String storageLocation) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank())
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            if (articleNumber != null && !articleNumber.isBlank())
                predicates.add(cb.like(cb.lower(root.get("articleNumber")), "%" + articleNumber.toLowerCase() + "%"));
            if (minStock != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("stockLevel"), minStock));
            if (storageLocation != null && !storageLocation.isBlank())
                predicates.add(cb.equal(root.get("storageLocation"), storageLocation));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
