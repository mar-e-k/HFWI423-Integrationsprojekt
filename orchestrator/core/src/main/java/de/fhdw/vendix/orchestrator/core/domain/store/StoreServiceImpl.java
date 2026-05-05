package de.fhdw.vendix.orchestrator.core.domain.store;

import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
class StoreServiceImpl extends AbstractCrudService<Store, Long> implements StoreService {

    private final StoreRepository storeRepository;

    StoreServiceImpl(StoreRepository storeRepository) {
        super(storeRepository);
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Store> findAllLockedStores() {
        return storeRepository.findAllLockedStores();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Store> findAllNonLockedStores() {
        return storeRepository.findAllNonLockedStores();
    }
}