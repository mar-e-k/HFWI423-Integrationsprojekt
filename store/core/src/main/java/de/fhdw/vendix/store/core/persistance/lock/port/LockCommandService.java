package de.fhdw.vendix.store.core.persistance.lock.port;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.TargetType;
import de.fhdw.vendix.commons.api.structure.service.CrudCommandService;

import java.util.UUID;

interface LockCommandService extends CrudCommandService<LockDTO, Long> {
    void deleteLockByTarget(TargetType targetType, long targetId);

    void deleteAllInstanceLocks(UUID instanceUUID);

    void deleteAllExpiredLocks();
}