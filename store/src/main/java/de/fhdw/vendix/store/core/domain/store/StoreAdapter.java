package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import org.springframework.stereotype.Service;

@Service
class StoreAdapter extends AbstractSpringDataCrudLogAdapter<Store, Long> {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    public StoreAdapter(StoreRepository storeRepository, StoreMapper storeMapper) {
        super(storeRepository);
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
    }
}