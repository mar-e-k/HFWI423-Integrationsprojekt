package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkLock;
import de.fhdw.fillialensystem.persistence.repository.StoreLinkLockRepository;
import de.fhdw.fillialensystem.persistence.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class StoreLinkLockService extends AbstractCrudService<StoreLinkLock, Long> {

    private final StoreRepository storeRepository;

    public StoreLinkLockService(StoreLinkLockRepository storeLinkLockRepository, StoreRepository storeRepository) {
        super(storeLinkLockRepository);
        this.storeRepository = storeRepository;
    }

    @Transactional
    public StoreLinkLock lockByStore(Store store) {
        StoreLinkLock storeLinkLock = new StoreLinkLock();
        storeLinkLock.setStore(store);
        storeLinkLock.setLockAcquiredAt(Instant.now());
        return repository.save(storeLinkLock);
    }

    @Transactional
    public StoreLinkLock lockByStoreId(Long storeId) {
        Store store = storeRepository.getReferenceById(storeId);
        return lockByStore(store);
    }

    @Transactional
    public void deleteByStore(Store store){
        ((StoreLinkLockRepository) repository).deleteByStore(store);
    }

    @Transactional
    public void deleteByStoreId(Long storeId){
        Store store = storeRepository.getReferenceById(storeId);
        deleteByStore(store);
    }
}