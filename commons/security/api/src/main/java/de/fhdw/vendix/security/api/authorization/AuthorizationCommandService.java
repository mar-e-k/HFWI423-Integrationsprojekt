package de.fhdw.vendix.security.api.authorization;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.TargetType;
import de.fhdw.vendix.commons.api.structure.service.CommandService;

import java.util.UUID;

interface AuthorizationCommandService extends CommandService {

    LockDTO createLock(LockDTO entity);

    void deleteLockByTarget(TargetType targetType, long targetId);

    void deleteAllInstanceLocks(UUID instanceUUID);

    void deleteAllExpiredLocks();
}