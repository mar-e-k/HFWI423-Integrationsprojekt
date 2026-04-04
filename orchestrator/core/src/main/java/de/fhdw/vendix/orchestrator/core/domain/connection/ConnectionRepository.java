package de.fhdw.vendix.orchestrator.core.domain.connection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    @Modifying
    @Query(
            """                
            DELETE
            FROM Connection c
            WHERE c.heartbeatAt < :cutOff
            """
    )
    void deleteAllExpiredConnections(@Param("cutoff") Integer cutoff);

    void deleteAllByInstance_Uuid(UUID instanceUuid);
}