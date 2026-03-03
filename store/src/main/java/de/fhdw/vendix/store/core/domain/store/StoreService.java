package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import org.springframework.stereotype.Service;

@Service
public class StoreService extends AbstractSpringDataCrudLogAdapter<Store, Long> {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper) {
        super(storeRepository);
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
    }
}