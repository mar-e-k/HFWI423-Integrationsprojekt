package de.fhdw.vendix.commons.api.domain.lock.port;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.LockRequestDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetType;
import de.fhdw.vendix.commons.api.structure.port.CommandPort;

import java.util.UUID;

public interface LockCommandPort extends CommandPort {
    LockDTO create(LockDTO dto);
    LockDTO create(LockRequestDTO dto);
    long deleteByTargetTypeAndTargetId(TargetType targetType, long targetId);
    long deleteAllByInstanceUUID(UUID instanceUUID);
}