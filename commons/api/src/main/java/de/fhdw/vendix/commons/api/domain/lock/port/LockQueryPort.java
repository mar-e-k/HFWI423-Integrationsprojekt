package de.fhdw.vendix.commons.api.domain.lock.port;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;

public interface LockQueryPort extends QueryPort {

    boolean existsByTargetTypeAndTargetID(TargetTypeEnum targetTypeEnum, Long id);

    Optional<LockDTO> findByTargetTypeAndTargetID(TargetTypeEnum targetTypeEnum, Long targetID);
}