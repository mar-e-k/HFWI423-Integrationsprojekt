package de.fhdw.vendix.commons.api.domain.lock.port;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockRequestDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.structure.port.CommandPort;

import java.util.UUID;

public interface LockCommandPort extends CommandPort {
    LockDTO create(LockRequestDTO entity);

    void deleteByTargetTypeAndTargetId(TargetTypeEnum targetTypeEnum, long targetId);

    void deleteAllByInstanceUUID(UUID instanceUUID);

    void deleteAllExpiredLocks();
}