package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.repository.StoreLinkHostRepository;
import de.fhdw.fillialensystem.persistence.repository.StoreLinkLockRepository;
import de.fhdw.fillialensystem.persistence.repository.StoreRepository;
import org.springframework.stereotype.Service;

@Service
public class StoreService extends AbstractCrudService<Store, Long> {

    private final StoreLinkLockRepository storeLinkLockRepository;
    private final StoreLinkHostRepository storeLinkHostRepository;

    public StoreService(StoreRepository storeRepository, StoreLinkLockRepository storeLinkLockRepository, StoreLinkHostRepository storeLinkHostRepository) {
        super(storeRepository);
        this.storeLinkLockRepository = storeLinkLockRepository;
        this.storeLinkHostRepository = storeLinkHostRepository;
    }

    //TODO:
    public void lockStore(Store store) {}

    public void unlockStore(Store store) {}

    public void pingStore(Store store) {}
}