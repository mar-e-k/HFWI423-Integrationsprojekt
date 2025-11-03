package com.example.application.services;

import com.example.application.data.ArticleInfo;
import com.example.application.data.ArticleInfoRepository;

import java.util.Optional;
import com.example.application.data.StockChangeLogRepository;
import org.springframework.transaction.annotation.Transactional;
import com.example.application.data.StockChangeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.example.application.data.ChangeType;

@Service
public class ArticleInfoService {

    private final ArticleInfoRepository repository;

    public Optional<ArticleInfo> get(Long id) {
        return repository.findById(id);
    }

    public ArticleInfo save(ArticleInfo entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<ArticleInfo> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<ArticleInfo> list(Pageable pageable, Specification<ArticleInfo> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public long count(Specification<ArticleInfo> filter) {
        return repository.count(filter);
    }

    public ArticleInfoService(ArticleInfoRepository repository,
                              StockChangeLogRepository logRepository) {
        this.repository = repository;
        this.logRepository = logRepository;
    }
    private final StockChangeLogRepository logRepository;
    @Transactional
    public ArticleInfo applyStockChange(ArticleInfo article,
                                        int delta,
                                        ChangeType type,
                                        String reason,
                                        String changedBy) {
        int oldStock = article.getStockLevel() == null ? 0 : article.getStockLevel();
        int newStock = oldStock + delta;
        article.setStockLevel(newStock);

        ArticleInfo saved = repository.save(article);

        StockChangeLog log = new StockChangeLog();
        log.setArticleId(saved.getId());
        log.setArticleNumber(saved.getArticleNumber());
        log.setArticleName(saved.getName());
        log.setOldStock(oldStock);
        log.setDelta(delta);
        log.setNewStock(newStock);
        log.setChangeType(type);      // Enum als String (per @Enumerated in Entity)
        log.setReason(reason);        // kann null sein
        log.setChangedBy(changedBy);  // kann null sein
        logRepository.save(log);

        return saved;
    }
}
