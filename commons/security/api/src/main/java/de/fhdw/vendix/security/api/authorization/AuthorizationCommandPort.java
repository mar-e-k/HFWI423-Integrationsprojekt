package de.fhdw.vendix.security.api.authorization;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.structure.port.CommandPort;

import java.util.UUID;

public interface AuthorizationCommandPort extends CommandPort {

    LockDTO createLock(LockDTO entity);

    void deleteLockByTarget(TargetTypeEnum targetTypeEnum, long targetId);

    void deleteAllInstanceLocks(UUID instanceUUID);

    void deleteAllExpiredLocks();
}