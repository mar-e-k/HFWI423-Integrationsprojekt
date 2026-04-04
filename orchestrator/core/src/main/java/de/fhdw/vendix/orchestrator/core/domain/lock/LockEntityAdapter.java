package de.fhdw.vendix.orchestrator.core.domain.lock;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
class LockEntityAdapter extends AbstractEntityCrudAdapter<Lock, Long> {

    private static final Logger log = LoggerFactory.getLogger(LockEntityAdapter.class);

    private final LockRepository lockRepository;

    LockEntityAdapter(LockRepository lockRepository) {
        super(lockRepository);
        this.lockRepository = lockRepository;
    }

    @Transactional(readOnly = true)
    public boolean existsByTarget(EntityTarget target) {
        if (target == null) {
            return false;
        }
        return lockRepository.existsByTarget(target);
    }

    @Transactional(readOnly = true)
    public Optional<Lock> findByTarget(EntityTarget target) {
        if (target == null) {
            return Optional.empty();
        }
        return lockRepository.findByTarget(target);
    }

    @Transactional(readOnly = true)
    public Set<Lock> findAllByInstanceUUID(UUID instanceUUID) {
        if (instanceUUID == null) {
            return Set.of();
        }
        return lockRepository.findAllByInstanceUUID(instanceUUID);
    }

    @Transactional
    public void deleteAllExpiredLocks() {
        log.atInfo().log("[DELETE] Deleting all expired locks");
        lockRepository.deleteAllExpiredLocks();
        log.atInfo().log("[DELETE] Successfully deleted all expired locks");
    }

    @Transactional
    public void deleteAllByInstanceUUID(UUID instanceUUID) {
        log.atInfo().log("[DELETE] Deleting all locks by instance");
        if (instanceUUID == null) {
            throw new IllegalArgumentException("Parameter 'instanceUUID' cannot be null");
        }
        lockRepository.deleteAllByInstanceUUID(instanceUUID);
        log.atInfo().log("[DELETE] Successfully deleted all locks by instance");
    }

    @Transactional
    public void deleteAllByTarget(EntityTarget target) {
        log.atInfo().log("[DELETE] Deleting all locks by target type and target id");
        if (target == null) {
            throw new IllegalArgumentException("Parameter 'target' cannot be null");
        }
        lockRepository.deleteAllByTarget(target);
        log.atInfo().log("[DELETE] Successfully deleted all locks by target type and target id");
    }
}