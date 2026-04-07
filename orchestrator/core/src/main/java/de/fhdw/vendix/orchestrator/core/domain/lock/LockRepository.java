package de.fhdw.vendix.orchestrator.core.domain.lock;

import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

interface LockRepository extends JpaRepository<Lock, Long> {

    Optional<Lock> findByTarget(EntityTarget target);

    Set<Lock> findAllByInstanceUUID(UUID instanceUUID);

    @Modifying
    @Query(
            """                
            DELETE
            FROM Lock
            WHERE expiresAt < CURRENT_TIMESTAMP
            """
    )
    void deleteAllExpiredLocks();

    void deleteByTarget(EntityTarget target);

    void deleteAllByInstanceUUID(UUID instanceUUID);
}