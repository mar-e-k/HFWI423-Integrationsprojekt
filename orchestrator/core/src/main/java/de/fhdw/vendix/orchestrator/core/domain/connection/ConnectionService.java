package de.fhdw.vendix.orchestrator.core.domain.connection;

import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.commons.api.structure.service.CrudService;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectionService extends CrudService<Connection, Long> {
    List<Connection> findAllByType(TargetType type);

    Optional<Connection> findByTarget(EntityTarget target);

    Optional<Connection> findByInstanceUUID(UUID instanceUuid);

    void deleteByTarget(EntityTarget target);

    void deleteByInstanceUUID(UUID instanceUuid);
}