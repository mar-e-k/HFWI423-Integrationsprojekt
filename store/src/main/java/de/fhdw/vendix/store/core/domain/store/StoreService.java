package de.fhdw.vendix.store.core.domain.store;

import de.fhdw.vendix.store.core.domain.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
public class StoreService extends AbstractCrudService<Store, Long> {

    public StoreService(StoreRepository storeRepository) {
        super(storeRepository);
    }
}