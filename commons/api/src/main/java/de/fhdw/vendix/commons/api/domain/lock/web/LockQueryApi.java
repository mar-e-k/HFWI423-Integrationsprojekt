package de.fhdw.vendix.commons.api.domain.lock.web;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.structure.web.QueryApi;

import java.util.Optional;

public interface LockQueryApi extends QueryApi {
    boolean existsByTarget(TargetTypeEnum targetTypeEnum, Long id);

    Optional<LockDTO> findByTarget(TargetTypeEnum targetTypeEnum, Long targetID);
}