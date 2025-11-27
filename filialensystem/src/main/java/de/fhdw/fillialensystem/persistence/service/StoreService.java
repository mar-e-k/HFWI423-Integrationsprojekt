package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.repository.StoreRepository;
import org.springframework.stereotype.Service;

@Service
public class StoreService extends AbstractCrudService<Store, Long> {

    public StoreService(StoreRepository storeRepository) {
        super(storeRepository);
    }
}