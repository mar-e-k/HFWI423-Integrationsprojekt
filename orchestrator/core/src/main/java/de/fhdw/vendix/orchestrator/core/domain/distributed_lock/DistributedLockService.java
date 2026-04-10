package de.fhdw.vendix.orchestrator.core.domain.distributed_lock;

import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.api.structure.service.CrudService;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DistributedLockService extends CrudService<DistributedLock, Long> {
    List<DistributedLock> findAllByTargetType(TargetType type);

    Optional<DistributedLock> findByTarget(EntityTarget target);

    List<DistributedLock> findAllByInstanceUUID(UUID instanceUUID);

    void deleteAllExpiredLocks();

    void deleteAllByInstanceUUID(UUID instanceUUID);

    void deleteByTarget(EntityTarget target);
}