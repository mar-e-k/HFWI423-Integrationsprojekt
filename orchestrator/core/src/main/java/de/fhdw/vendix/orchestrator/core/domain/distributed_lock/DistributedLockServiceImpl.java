package de.fhdw.vendix.orchestrator.core.domain.distributed_lock;

import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class DistributedLockServiceImpl extends AbstractEntityCrudAdapter<DistributedLock, Long> implements DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockServiceImpl.class);

    private final DistributedLockRepository distributedLockRepository;

    DistributedLockServiceImpl(DistributedLockRepository distributedLockRepository) {
        super(distributedLockRepository);
        this.distributedLockRepository = distributedLockRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistributedLock> findAllByTargetType(TargetType type) {
        if (type == null) {
            return List.of();
        }
        return distributedLockRepository.findAllByTarget_Type(type);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DistributedLock> findByTarget(EntityTarget target) {
        if (target == null) {
            return Optional.empty();
        }
        return distributedLockRepository.findByTarget(target);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistributedLock> findAllByInstanceUUID(UUID instanceUUID) {
        if (instanceUUID == null) {
            return List.of();
        }
        return distributedLockRepository.findAllByInstanceUUID(instanceUUID);
    }

    // TODO: might need more proper logging, where you grab each instance beforehand

    @Override
    @Transactional
    public void deleteAllExpiredLocks() {
        log.atInfo().log("[DELETE] Deleting all expired locks");
        distributedLockRepository.deleteAllExpiredLocks();
        log.atInfo().log("[DELETE] Successfully deleted all expired locks");
    }

    @Override
    @Transactional
    public void deleteAllByInstanceUUID(UUID instanceUUID) {
        log.atInfo().log("[DELETE] Deleting all locks by instance");
        if (instanceUUID == null) {
            throw new IllegalArgumentException("Parameter 'instanceUuid' cannot be null");
        }
        distributedLockRepository.deleteAllByInstanceUUID(instanceUUID);
        log.atInfo().log("[DELETE] Successfully deleted all locks by instance");
    }

    @Override
    @Transactional
    public void deleteByTarget(EntityTarget target) {
        log.atInfo().log("[DELETE] Deleting all locks by target type and target id");
        if (target == null) {
            throw new IllegalArgumentException("Parameter 'target' cannot be null");
        }
        distributedLockRepository.deleteByTarget(target);
        log.atInfo().log("[DELETE] Successfully deleted all locks by target type and target id");
    }
}