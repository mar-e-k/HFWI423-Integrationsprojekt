package de.fhdw.vendix.orchestrator.core.domain.lock.service;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudCommandService;

import java.util.UUID;

interface LockCommandService extends CrudCommandService<LockDTO, Long> {
    void deleteLockByTarget(EntityTargetDTO target);

    void deleteAllInstanceLocks(UUID instanceUUID);

    void deleteAllExpiredLocks();
}