package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
class StoreStockEntityAdapter extends AbstractEntityCrudAdapter<StoreStock, Long> {

    private final StoreStockRepository storeStockRepository;

    StoreStockEntityAdapter(StoreStockRepository storeStockRepository) {
        super(storeStockRepository);
        this.storeStockRepository = storeStockRepository;
    }

    @Transactional
    public void restockArticle(long storeID, long articleID, long articleQuantity) {
        StoreStock storeStock = storeStockRepository.findByStoreAndArticle(storeID, articleID)
                .orElseThrow(EntityNotFoundException::new);
        storeStock.restockArticle(articleQuantity);
        super.update(storeStock);
    }

    @Transactional(readOnly = true)
    public Optional<StoreStock> findByStoreAndArticle(Long storeId, Long articleId) {
        if (storeId == null || storeId < 0) {
            return Optional.empty();
        }
        if (articleId == null || articleId < 0) {
            return Optional.empty();
        }
        return storeStockRepository.findByStoreAndArticle(storeId, articleId);
    }
}