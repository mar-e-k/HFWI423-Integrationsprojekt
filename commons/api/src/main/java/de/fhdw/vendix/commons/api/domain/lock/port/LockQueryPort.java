package de.fhdw.vendix.commons.api.domain.lock.port;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetType;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;

public interface LockQueryPort extends QueryPort {
    Optional<LockDTO> findByTargetTypeAndTargetID(TargetType targetType, long targetID);

    boolean existsByTargetTypeAndTargetID(TargetType targetType, long id);
}