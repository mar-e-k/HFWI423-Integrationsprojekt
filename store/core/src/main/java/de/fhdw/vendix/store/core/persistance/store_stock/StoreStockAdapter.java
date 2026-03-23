package de.fhdw.vendix.store.core.persistance.store_stock;

import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.port.StoreStockCommandPort;
import de.fhdw.vendix.commons.api.domain.store_stock.port.StoreStockQueryPort;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class StoreStockAdapter extends AbstractDtoCrudAdapter<StoreStock, StoreStockDTO, Long> implements StoreStockQueryPort, StoreStockCommandPort {

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