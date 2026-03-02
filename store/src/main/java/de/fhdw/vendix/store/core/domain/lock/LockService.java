package de.fhdw.vendix.store.core.domain.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetType;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class LockService extends AbstractSpringDataCrudLogAdapter<Lock, Long> implements LockCommandPort, LockQueryPort {

    private final LockRepository lockRepository;
    private final LockMapper lockMapper;

    public LockService(LockRepository lockRepository, LockMapper lockMapper) {
        super(lockRepository);
        this.lockRepository = lockRepository;
        this.lockMapper = lockMapper;
    }

    @Override
    public void deleteByTargetTypeAndTargetId(TargetType targetType, long targetId) {
        lockRepository.deleteByTargetTypeAndTargetId(targetType, targetId);
    }

    @Override
    public void deleteAllByInstanceUUID(UUID instanceUUID) {
        lockRepository.deleteAllByInstanceUUID(instanceUUID);
    }

    @Override
    public void deleteAllExpiredLocks() {
        lockRepository.deleteAllExpiredLocks();
    }

    @Override
    public Optional<LockDTO> findByTargetTypeAndTargetID(TargetType targetType, long targetID) {
        if (targetType == null) {
            throw new IllegalArgumentException("Parameter 'targetType' cannot be null.");
        }
        return lockRepository.findByTargetTypeAndTargetId(targetType, targetID)
                .map(lockMapper::toDTO);
    }

    @Override
    public boolean existsByTargetTypeAndTargetID(TargetType targetType, long id) {
        if (targetType == null) {
            throw new IllegalArgumentException("Parameter 'targetType' cannot be null.");
        }
        return lockRepository.existsByTargetTypeAndTargetId(targetType, id);
    }
}