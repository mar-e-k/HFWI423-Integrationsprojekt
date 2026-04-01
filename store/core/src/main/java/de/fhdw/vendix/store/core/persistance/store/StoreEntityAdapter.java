package de.fhdw.vendix.store.core.persistance.store;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import de.fhdw.vendix.store.core.persistance.register.Register;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
class StoreEntityAdapter extends AbstractEntityCrudAdapter<Store, Long> {

    private final StoreRepository storeRepository;

    StoreEntityAdapter(StoreRepository storeRepository) {
        super(storeRepository);
        this.storeRepository = storeRepository;
    }

    public Set<Store> findAllActiveStores() {
        return storeRepository.findAllActiveStores();
    }

    public Set<Store> findAllInactiveStores() {
        return storeRepository.findAllInactiveStores();
    }

    public Set<Register> findAllRegisters(long storeId) {
        return storeRepository.findAllRegisters(storeId);
    }
}