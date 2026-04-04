package de.fhdw.vendix.orchestrator.core.domain.lock;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTargetMapper;
import de.fhdw.vendix.orchestrator.core.domain.lock.service.LockService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class LockAdapter extends AbstractDtoCrudAdapter<Lock, LockDTO, Long> implements LockService {

    private final LockEntityAdapter lockEntityAdapter;
    private final LockMapper lockMapper;
    private final EntityTargetMapper entityTargetMapper;

    LockAdapter(LockEntityAdapter lockEntityAdapter, LockMapper lockMapper, EntityTargetMapper entityTargetMapper) {
        super(lockEntityAdapter, lockMapper);
        this.lockEntityAdapter = lockEntityAdapter;
        this.lockMapper = lockMapper;
        this.entityTargetMapper = entityTargetMapper;
    }

    @Override
    public boolean existsByTarget(EntityTargetDTO target) {
        if (target == null) {
            return false;
        }
        return lockEntityAdapter.existsByTarget(
                entityTargetMapper.toEntity(target));
    }

    @Override
    public Optional<LockDTO> findByTarget(EntityTargetDTO target) {
        if (target == null) {
            return Optional.empty();
        }
        return lockEntityAdapter.findByTarget(entityTargetMapper.toEntity(target))
                .map(lockMapper::toDTO);
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
        lockEntityAdapter.deleteAllExpiredLocks();
    }

    @Override
    public void deleteAllInstanceLocks(UUID instanceUUID) {
        if (instanceUUID == null) {
            throw new IllegalArgumentException("Parameter 'instanceUUID' cannot be null");
        }
        lockEntityAdapter.deleteAllByInstanceUUID(instanceUUID);
    }

    @Override
    public void deleteLockByTarget(EntityTargetDTO target) {
        if (target == null) {
            throw new IllegalArgumentException("Parameter 'target' cannot be null");
        }
        lockEntityAdapter.deleteAllByTarget(entityTargetMapper.toEntity(target));
    }
}