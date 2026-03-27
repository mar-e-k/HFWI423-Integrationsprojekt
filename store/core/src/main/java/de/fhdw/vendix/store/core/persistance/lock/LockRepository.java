package de.fhdw.vendix.store.core.persistance.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

interface LockRepository extends JpaRepository<Lock, Long> {

    boolean existsByTargetTypeAndTargetId(TargetTypeEnum targetTypeEnum, long targetId);

    Optional<Lock> findByTargetTypeAndTargetId(TargetTypeEnum targetTypeEnum, long targetId);

    Set<Lock> findAllByTargetType(TargetTypeEnum targetType);

    Set<Lock> findAllByTargetId(long targetId);

    Set<Lock> findAllByInstanceUUID(UUID instanceUUID);

    @Modifying
    @Query(
                            """                
                            DELETE
                            FROM Lock
                            WHERE createdAt < CURRENT_TIMESTAMP
                            """
    )
    void deleteAllByExpiresAtNow();

    void deleteAllByInstanceUUID(UUID instanceUUID);

    void deleteAllByTargetTypeAndTargetId(TargetTypeEnum targetType, long targetId);
}