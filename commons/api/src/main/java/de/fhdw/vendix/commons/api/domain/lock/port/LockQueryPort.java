package de.fhdw.vendix.commons.api.domain.lock.port;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;

public interface LockQueryPort extends QueryPort {
    Optional<LockDTO> findByTargetTypeAndTargetID(TargetTypeEnum targetTypeEnum, long targetID);

    boolean existsByTargetTypeAndTargetID(TargetTypeEnum targetTypeEnum, long id);
}