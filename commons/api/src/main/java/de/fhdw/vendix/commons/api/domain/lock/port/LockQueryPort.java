package de.fhdw.vendix.commons.api.domain.lock.port;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface LockQueryPort extends QueryPort {
    Optional<LockDTO> findByTarget(TargetTypeEnum targetTypeEnum, Long targetID);

    Set<LockDTO> findAllByTargetType(TargetTypeEnum targetType);

    Set<LockDTO> findAllByTargetId(long targetId);

    Set<LockDTO> findAllByInstanceUUID(UUID instanceUUID);
}