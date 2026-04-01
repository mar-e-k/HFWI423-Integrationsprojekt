package de.fhdw.vendix.store.core.persistance.lock.port;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.TargetType;
import de.fhdw.vendix.commons.api.structure.service.CrudQueryService;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

interface LockQueryService extends CrudQueryService<LockDTO, Long> {
    Optional<LockDTO> findByTarget(TargetType targetType, Long targetID);

    Set<LockDTO> findAllByTargetType(TargetType targetType);

    Set<LockDTO> findAllByTargetId(long targetId);

    Set<LockDTO> findAllByInstanceUUID(UUID instanceUUID);
}