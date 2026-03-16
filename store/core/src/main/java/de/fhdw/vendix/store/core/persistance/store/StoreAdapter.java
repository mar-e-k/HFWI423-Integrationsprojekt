package de.fhdw.vendix.store.core.persistance.store;

import de.fhdw.vendix.commons.spring.core.crud.AbstractCrudLogAdapter;
import org.springframework.stereotype.Service;

@Service
class StoreAdapter extends AbstractCrudLogAdapter<Store, Long> {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    public StoreAdapter(StoreRepository storeRepository, StoreMapper storeMapper) {
        super(storeRepository);
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
    }
}