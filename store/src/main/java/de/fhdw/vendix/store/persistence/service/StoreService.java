package de.fhdw.vendix.store.persistence.service;

import de.fhdw.vendix.store.persistence.entity.Store;
import de.fhdw.vendix.store.persistence.repository.StoreRepository;
import org.springframework.stereotype.Service;

@Service
public class StoreService extends AbstractCrudService<Store, Long> {

    public StoreService(StoreRepository storeRepository) {
        super(storeRepository);
    }
}