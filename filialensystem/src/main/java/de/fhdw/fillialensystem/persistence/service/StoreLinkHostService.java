package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkHost;
import de.fhdw.fillialensystem.persistence.repository.StoreLinkHostRepository;
import de.fhdw.fillialensystem.persistence.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreLinkHostService extends AbstractCrudService<StoreLinkHost, Long> {

    private final StoreRepository storeRepository;

    public StoreLinkHostService(StoreLinkHostRepository storeLinkHostRepository, StoreRepository storeRepository) {
        super(storeLinkHostRepository);
        this.storeRepository = storeRepository;
    }

    @Transactional
    public void deleteByStore(Store store){
        ((StoreLinkHostRepository) repository).deleteByStore(store);
    }

    @Transactional
    public void deleteByStoreId(Long storeId){
        Store store = storeRepository.getReferenceById(storeId);
        deleteByStore(store);
    }
}