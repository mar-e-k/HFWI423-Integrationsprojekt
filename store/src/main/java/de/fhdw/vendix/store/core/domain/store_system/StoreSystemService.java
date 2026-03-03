package de.fhdw.vendix.store.core.domain.store_system;

import de.fhdw.vendix.store.core.domain.AbstractCrudService;
import de.fhdw.vendix.store.core.domain.store.Store;
import de.fhdw.vendix.store.core.domain.store.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreSystemService extends AbstractCrudService<StoreSystem, Long> {

    private final StoreRepository storeRepository;

    public StoreSystemService(StoreSystemRepository storeSystemRepository, StoreRepository storeRepository) {
        super(storeSystemRepository);
        this.storeRepository = storeRepository;
    }

    @Transactional
    public void deleteByStore(Store store){
        ((StoreSystemRepository) repository).deleteByStore(store);
    }

    @Transactional
    public void deleteByStoreId(Long storeId){
        Store store = storeRepository.getReferenceById(storeId);
        deleteByStore(store);
    }
}