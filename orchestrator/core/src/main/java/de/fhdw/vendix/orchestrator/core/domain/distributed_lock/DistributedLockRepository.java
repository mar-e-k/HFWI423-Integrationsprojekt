package de.fhdw.vendix.orchestrator.core.domain.distributed_lock;

import de.fhdw.vendix.commons.api.embeddable.TargetType;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface DistributedLockRepository extends JpaRepository<DistributedLock, Long> {

    List<DistributedLock> findAllByTarget_Type(TargetType targetType);

    Optional<DistributedLock> findByTarget(EntityTarget target);

    List<DistributedLock> findAllByInstanceUuid(UUID instanceUuid);

    @Modifying
    @Query(
            """                
            DELETE
            FROM DistributedLock
            WHERE expiresAt < CURRENT_TIMESTAMP
            """
    )
    void deleteAllExpiredLocks();

    void deleteByTarget(EntityTarget target);

    void deleteAllByInstanceUuid(UUID instanceUuid);
}