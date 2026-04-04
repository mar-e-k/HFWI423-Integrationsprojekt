package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.api.domain.store_stock.StoreStockDTO;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.store.core.domain.store_stock.service.StoreStockService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class StoreStockAdapter extends AbstractDtoCrudAdapter<StoreStock, StoreStockDTO, Long> implements StoreStockService {

    private final StoreStockEntityAdapter storeStockEntityAdapter;
    private final StoreStockMapper storeStockMapper;

    StoreStockAdapter(StoreStockEntityAdapter storeStockEntityAdapter, StoreStockMapper storeStockMapper) {
        super(storeStockEntityAdapter, storeStockMapper);
        this.storeStockEntityAdapter = storeStockEntityAdapter;
        this.storeStockMapper = storeStockMapper;
    }


    @Override
    public void restockArticle(Long storeID, Long articleID, Long articleQuantity) {
        storeStockEntityAdapter.restockArticle(storeID, articleID, articleQuantity);
    }

    @Override
    public Optional<StoreStockDTO> findByStoreIDAndArticleID(Long storeID, Long articleID) {
        return storeStockEntityAdapter.findByStoreAndArticle(storeID, articleID)
                .map(storeStockMapper::toDTO);
    }
}