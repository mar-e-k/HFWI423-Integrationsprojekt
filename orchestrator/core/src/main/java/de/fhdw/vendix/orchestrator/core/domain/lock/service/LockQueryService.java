package de.fhdw.vendix.orchestrator.core.domain.lock.service;

import de.fhdw.vendix.commons.api.domain.lock.LockDTO;
import de.fhdw.vendix.commons.api.embeddable.EntityTargetDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudQueryService;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

interface LockQueryService extends CrudQueryService<LockDTO, Long> {

    boolean existsByTarget(EntityTargetDTO target);

    Optional<LockDTO> findByTarget(EntityTargetDTO target);

    Set<LockDTO> findAllByInstanceUUID(UUID instanceUUID);
}