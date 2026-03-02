package de.fhdw.vendix.commons.api.domain.lock.port;

import de.fhdw.vendix.commons.api.domain.lock.dto.TargetType;
import de.fhdw.vendix.commons.api.structure.port.CommandPort;

import java.util.UUID;

public interface LockCommandPort extends CommandPort {
    void deleteByTargetTypeAndTargetId(TargetType targetType, long targetId);

    void deleteAllByInstanceUUID(UUID instanceUUID);

    void deleteAllExpiredLocks();
}