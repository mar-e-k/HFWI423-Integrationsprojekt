package de.fhdw.vendix.orchestrator.core.domain.connection;

import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    Optional<Connection> findByTarget(EntityTarget target);

    Optional<Connection> findByInstance_Uuid(UUID instanceUuid);

    void deleteByTarget(EntityTarget target);

    void deleteByInstance_Uuid(UUID instanceUuid);
}