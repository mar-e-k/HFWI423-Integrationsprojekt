package de.fhdw.vendix.commons.spring.security.authorization;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.structure.service.CommandService;

import java.util.UUID;

interface AuthorizationCommandService extends CommandService {

    LockDTO createLock(LockDTO entity);

    void deleteLockByTarget(EntityTargetDTO target);

    void deleteAllInstanceLocks(UUID instanceUUID);

    void deleteAllExpiredLocks();
}