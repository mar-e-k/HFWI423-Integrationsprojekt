package de.fhdw.vendix.store.core.persistance.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class LockAdapter extends AbstractDtoCrudAdapter<Lock, LockDTO, Long> implements LockQueryPort, LockCommandPort {

    private final LockEntityAdapter lockEntityAdapter;
    private final LockMapper lockMapper;

    LockAdapter(LockEntityAdapter lockEntityAdapter, LockMapper lockMapper) {
        super(lockEntityAdapter, lockMapper);
        this.lockEntityAdapter = lockEntityAdapter;
        this.lockMapper = lockMapper;
    }

    @Override
    public boolean existsByTarget(TargetTypeEnum targetTypeEnum, Long targetId) {
        if (targetTypeEnum == null) {
            return false;
        }
        if (targetId < 0) {
            return false;
        }
        return lockEntityAdapter.existsByTargetTypeAndTargetId(targetTypeEnum, targetId);
    }

    @Override
    public Optional<LockDTO> findByTarget(TargetTypeEnum targetTypeEnum, Long targetId) {
        if (targetTypeEnum == null) {
            return Optional.empty();
        }
        if (targetId < 0) {
            return Optional.empty();
        }
        return lockEntityAdapter.findByTargetTypeAndTargetId(targetTypeEnum, targetId)
                .map(lockMapper::toDTO);
    }

    @Override
    public void deleteAllExpiredLocks() {
        lockEntityAdapter.deleteAllByExpiresAtNow();
    }

    @Override
    public void deleteAllInstanceLocks(UUID instanceUUID) {
        if (instanceUUID == null) {
            throw new IllegalArgumentException("Parameter 'instanceUUID' cannot be null");
        }
        lockEntityAdapter.deleteAllByInstanceUUID(instanceUUID);
    }

    @Override
    public void deleteLockByTarget(TargetTypeEnum targetTypeEnum, long targetId) {
        if (targetTypeEnum == null) {
            throw new IllegalArgumentException("Parameter 'targetType' cannot be null");
        }
        if (targetId < 0) {
            throw new IllegalArgumentException("Parameter 'targetId' cannot be negative");
        }
        lockEntityAdapter.deleteAllByTargetTypeAndTargetId(targetTypeEnum, targetId);
    }
}