package de.fhdw.vendix.orchestrator.core.domain.lock;

import de.fhdw.vendix.commons.api.structure.service.CrudService;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface LockService extends CrudService<Lock, Long> {
    Optional<Lock> findByTarget(EntityTarget target);

    Set<Lock> findAllByInstanceUUID(UUID instanceUUID);

    void deleteAllExpiredLocks();

    void deleteAllByInstanceUUID(UUID instanceUUID);

    void deleteByTarget(EntityTarget target);
}