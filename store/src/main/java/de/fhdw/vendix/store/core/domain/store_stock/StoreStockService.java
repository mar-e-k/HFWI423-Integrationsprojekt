package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.api.domain.store_stock.port.StoreStockCommandPort;
import de.fhdw.vendix.commons.api.domain.store_stock.port.StoreStockQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import de.fhdw.vendix.store.core.domain.store.StoreMapper;
import org.springframework.stereotype.Service;

@Service
public class StoreStockService extends AbstractSpringDataCrudLogAdapter<StoreStock, Long> implements StoreStockQueryPort, StoreStockCommandPort {

   private final StoreStockRepository storeStockRepository;
   private final StoreMapper storeMapper;

    public StoreStockService(StoreStockRepository storeStockRepository, StoreMapper storeMapper) {
        super(storeStockRepository);
        this.storeStockRepository = storeStockRepository;
        this.storeMapper = storeMapper;
    }
}