package de.fhdw.vendix.store.core.persistance.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
class LockEntityAdapter extends AbstractEntityCrudAdapter<Lock, Long> {

    private final LockRepository lockRepository;

    LockEntityAdapter(LockRepository lockRepository) {
        super(lockRepository);
        this.lockRepository = lockRepository;
    }

    @Transactional(readOnly = true)
    public boolean existsByTargetTypeAndTargetId(TargetTypeEnum targetTypeEnum, long targetId) {
        if (targetTypeEnum == null) {
            return false;
        }
        if (targetId < 0L) {
            return false;
        }
        return lockRepository.existsByTargetTypeAndTargetId(targetTypeEnum, targetId);
    }

    @Transactional(readOnly = true)
    public Optional<Lock> findByTargetTypeAndTargetId(TargetTypeEnum targetTypeEnum, long targetId) {
        if (targetTypeEnum == null) {
            return Optional.empty();
        }
        if (targetId < 0L) {
            return Optional.empty();
        }
        return lockRepository.findByTargetTypeAndTargetId(targetTypeEnum, targetId);
    }

    @Transactional
    public void deleteAllByExpiresAtNow() {
        lockRepository.deleteAllByExpiresAtNow();
    }

    @Transactional
    public void deleteAllByInstanceUUID(UUID instanceUUID) {
        if (instanceUUID == null) {
            throw new IllegalArgumentException("Parameter 'instanceUUID' cannot be null");
        }
        lockRepository.deleteAllByInstanceUUID(instanceUUID);
    }

    @Transactional
    public void deleteAllByTargetTypeAndTargetId(TargetTypeEnum targetType, long targetId) {
        if (targetType == null) {
            throw new IllegalArgumentException("Parameter 'targetType' cannot be null");
        }
        if (targetId < 0L) {
            throw new IllegalArgumentException("Parameter 'targetId' cannot be negative");
        }
        lockRepository.deleteAllByTargetTypeAndTargetId(targetType, targetId);
    }
}