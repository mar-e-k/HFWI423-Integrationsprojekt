package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.spring.data.crud.AbstractCrudService;
import de.fhdw.vendix.store.core.embeddable.preference_amount.PreferenceAmount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
class StoreStockServiceImpl extends AbstractCrudService<StoreStock, Long> implements StoreStockService {

    private static final Logger log = LoggerFactory.getLogger(StoreStockServiceImpl.class);

    private final StoreStockRepository storeStockRepository;

    StoreStockServiceImpl(StoreStockRepository storeStockRepository) {
        super(storeStockRepository);
        this.storeStockRepository = storeStockRepository;
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