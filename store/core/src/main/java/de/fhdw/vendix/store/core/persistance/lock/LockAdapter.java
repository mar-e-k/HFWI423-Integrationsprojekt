package de.fhdw.vendix.store.core.persistance.lock;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.TargetType;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.store.core.persistance.lock.port.LockService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class LockAdapter extends AbstractDtoCrudAdapter<Lock, LockDTO, Long> implements LockService {

    private final LockEntityAdapter lockEntityAdapter;
    private final LockMapper lockMapper;

    LockAdapter(LockEntityAdapter lockEntityAdapter, LockMapper lockMapper) {
        super(lockEntityAdapter, lockMapper);
        this.lockEntityAdapter = lockEntityAdapter;
        this.lockMapper = lockMapper;
    }

    @Override
    public Optional<LockDTO> findByTarget(TargetType targetType, Long targetId) {
        if (targetType == null) {
            return Optional.empty();
        }
        if (targetId == null || targetId < 0) {
            return Optional.empty();
        }
        return lockEntityAdapter.findByTargetTypeAndTargetId(targetType, targetId)
                .map(lockMapper::toDTO);
    }

    @Override
    public Set<LockDTO> findAllByTargetType(TargetType targetType) {
        if (targetType == null) {
            return Set.of();
        }
        return lockEntityAdapter.findAllByTargetType(targetType).stream()
                .map(lockMapper::toDTO)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<LockDTO> findAllByTargetId(long targetId) {
        if (targetId < 0) {
            return Set.of();
        }
        return lockEntityAdapter.findAllByTargetId(targetId).stream()
                .map(lockMapper::toDTO)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<LockDTO> findAllByInstanceUUID(UUID instanceUUID) {
        if (instanceUUID == null) {
            return Set.of();
        }
        return lockEntityAdapter.findAllByInstanceUUID(instanceUUID).stream()
                .map(lockMapper::toDTO)
                .collect(Collectors.toSet());
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
    public void deleteLockByTarget(TargetType targetType, long targetId) {
        if (targetType == null) {
            throw new IllegalArgumentException("Parameter 'targetType' cannot be null");
        }
        if (targetId < 0) {
            throw new IllegalArgumentException("Parameter 'targetId' cannot be negative");
        }
        lockEntityAdapter.deleteAllByTargetTypeAndTargetId(targetType, targetId);
    }
}