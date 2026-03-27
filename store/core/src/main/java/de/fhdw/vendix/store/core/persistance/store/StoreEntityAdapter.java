package de.fhdw.vendix.store.core.persistance.store;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
class StoreEntityAdapter extends AbstractEntityCrudAdapter<Store, Long> {

    private final StoreRepository storeRepository;

    StoreEntityAdapter(StoreRepository storeRepository) {
        super(storeRepository);
        this.storeRepository = storeRepository;
    }

    public Set<Store> getAllActiveStores() {
        return storeRepository.findAllActiveStores();
    }

    public Set<Store> getAllInactiveStores() {
        return storeRepository.findAllInactiveStores();
    }
}