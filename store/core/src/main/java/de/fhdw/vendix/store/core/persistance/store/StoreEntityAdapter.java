package de.fhdw.vendix.store.core.persistance.store;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class StoreEntityAdapter extends AbstractEntityCrudAdapter<Store, Long> {

    private final StoreRepository storeRepository;

    StoreEntityAdapter(StoreRepository storeRepository) {
        super(storeRepository);
        this.storeRepository = storeRepository;
    }
}