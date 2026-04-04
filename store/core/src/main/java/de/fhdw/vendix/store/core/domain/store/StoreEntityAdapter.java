package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import de.fhdw.vendix.store.core.domain.register.Register;
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
        return Set.of();
    }

    public Set<Store> findAllInactiveStores() {
        return Set.of();
    }

    public Set<Register> findAllRegisters(long storeId) {
        return storeRepository.findAllRegisters(storeId);
    }
}