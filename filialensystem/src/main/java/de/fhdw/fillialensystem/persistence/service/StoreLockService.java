package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.StoreLock;
import de.fhdw.fillialensystem.persistence.repository.StoreLockRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class StoreLockService extends AbstractCrudService<StoreLock, Long> {

    private final StoreLockRepository storeLockRepository;

    public StoreLockService(StoreLockRepository storeLockRepository) {
        super(storeLockRepository);
        this.storeLockRepository = storeLockRepository;
    }

    public Optional<StoreLock> findByStoreId(String storeId) {
        return storeLockRepository.findByStoreId(storeId);
    }

    @Transactional
    public void deleteByStoreId(String storeId) {
        storeLockRepository.deleteByStoreId(storeId);
    }

    public boolean isInstanceLocked(String instanceId) {
        return storeLockRepository.existsByLockedByInstanceId(instanceId);
    }

    @Transactional
    public StoreLock lock(String storeId, String instanceId) {
        if (findByStoreId(storeId).isPresent()) {
            throw new IllegalStateException("Store with id " + storeId + " is already locked.");
        }
        StoreLock lock = new StoreLock(storeId, instanceId, Instant.now());
        try {
            return save(lock);
        } catch (DataIntegrityViolationException ex) {
            // DB unique constraint has triggered -> treat as already locked
            throw new IllegalStateException("Store with id " + storeId + " is already locked.", ex);
        }
    }
}
