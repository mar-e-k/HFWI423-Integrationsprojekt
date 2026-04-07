package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
class StoreStockServiceImpl extends AbstractEntityCrudAdapter<StoreStock, Long> implements StoreStockService {

    private final StoreStockRepository storeStockRepository;

    StoreStockServiceImpl(StoreStockRepository storeStockRepository) {
        super(storeStockRepository);
        this.storeStockRepository = storeStockRepository;
    }

    @Override
    @Transactional
    public void restockArticle(Long storeID, Long articleID, Long articleQuantity) {
        if (storeID == null || storeID < 0) {
            throw new IllegalArgumentException("Parameter 'storeID' cannot be null or negative");
        }
        if (articleID == null || articleID < 0) {
            throw new IllegalArgumentException("Parameter 'articleID' cannot be null or negative");
        }
        if (articleQuantity == null || articleQuantity < 0) {
            throw new IllegalArgumentException("Parameter 'articleQuantity' cannot be null or negative");
        }

        StoreStock storeStock = storeStockRepository.findByStore_IdAndArticle_Id(storeID, articleID)
                .orElseThrow(EntityNotFoundException::new);
        storeStock.restockArticle(articleQuantity);

        super.update(storeStock);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoreStock> findByStoreIDAndArticleID(Long storeId, Long articleId) {
        if (storeId == null || storeId < 0) {
            return Optional.empty();
        }
        if (articleId == null || articleId < 0) {
            return Optional.empty();
        }

        return storeStockRepository.findByStore_IdAndArticle_Id(storeId, articleId);
    }
}