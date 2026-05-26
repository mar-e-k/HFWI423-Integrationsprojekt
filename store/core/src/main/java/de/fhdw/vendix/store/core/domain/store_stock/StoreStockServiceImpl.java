package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.spring.data.crud.AbstractCrudService;
import de.fhdw.vendix.store.core.embeddable.preference_amount.PreferenceAmount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
class StoreStockServiceImpl extends AbstractCrudService<StoreStock, Long> implements StoreStockService {

    private static final Logger log = LoggerFactory.getLogger(StoreStockServiceImpl.class);

    private final StoreStockRepository storeStockRepository;
    private final StoreStockBulkRepository storeStockBulkRepository;

    StoreStockServiceImpl(
            StoreStockRepository storeStockRepository,
            StoreStockBulkRepository storeStockBulkRepository
    ) {
        super(storeStockRepository);
        this.storeStockRepository = storeStockRepository;
        this.storeStockBulkRepository = storeStockBulkRepository;
    }

    @Override
    @Transactional
    public void restockArticle(Long storeId, Long articleId, Long articleQuantity) {
        log.atInfo().log("Restocking article '{}' of store '{}' with amount '{}'", articleId, storeId, articleQuantity);
        if (storeId == null || storeId < 0) {
            throw new IllegalArgumentException("Parameter 'storeID' cannot be null or negative");
        }
        if (articleId == null || articleId < 0) {
            throw new IllegalArgumentException("Parameter 'articleID' cannot be null or negative");
        }
        if (articleQuantity == null || articleQuantity < 0) {
            throw new IllegalArgumentException("Parameter 'articleQuantity' cannot be null or negative");
        }

        StoreStock storeStock = storeStockRepository.findByStoreIdAndArticleId(storeId, articleId)
                .orElseGet(() -> createMissingArticle(storeId, articleId));
        storeStock.restockArticle(articleQuantity);

        super.update(storeStock);
        log.atInfo().log("Successfully restocked article '{}' of store '{}' with amount '{}'", articleId, storeId, articleQuantity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoreStock> findByStoreIdAndArticleId(Long storeId, Long articleId) {
        if (storeId == null || storeId < 0) {
            return Optional.empty();
        }
        if (articleId == null || articleId < 0) {
            return Optional.empty();
        }

        return storeStockRepository.findByStoreIdAndArticleId(storeId, articleId);
    }

    @Override
    @Transactional
    public void decrementArticles(Long storeId, Map<Long, Long> amountByArticleId) {
        validateArticleAmounts(storeId, amountByArticleId);
        storeStockBulkRepository.decrementArticles(storeId, amountByArticleId);
    }

    @Override
    @Transactional
    public void incrementArticles(Long storeId, Map<Long, Long> amountByArticleId) {
        validateArticleAmounts(storeId, amountByArticleId);
        storeStockBulkRepository.incrementArticles(storeId, amountByArticleId);
    }

    private void validateArticleAmounts(Long storeId, Map<Long, Long> amountByArticleId) {
        if (storeId == null || storeId < 1) {
            throw new IllegalArgumentException("Parameter 'storeId' cannot be null or less than 1");
        }
        if (amountByArticleId == null || amountByArticleId.isEmpty()) {
            return;
        }
        amountByArticleId.forEach((articleId, amount) -> {
            if (articleId == null || articleId < 1) {
                throw new IllegalArgumentException("Parameter 'articleId' cannot be null or less than 1");
            }
            if (amount == null || amount < 1) {
                throw new IllegalArgumentException("Parameter 'amount' cannot be null or less than 1");
            }
        });
    }

    private StoreStock createMissingArticle(Long storeId, Long articleId) {
        log.atInfo().log("Creating missing article '{}' in store '{}'...", articleId, storeId);
        PreferenceAmount preferenceAmount = new PreferenceAmount(
                10L,
                50L,
                100L
        );
        StoreStock storeStock = new StoreStock(
                storeId,
                articleId,
                0L,
                preferenceAmount
        );
        StoreStock created = super.create(storeStock);
        log.atInfo().log("Successfully created missing article '{}' in store '{}'", articleId, storeId);
        return created;
    }
}
