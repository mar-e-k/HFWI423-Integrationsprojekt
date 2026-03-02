package de.fhdw.vendix.store.core.domain.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.TargetType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface LockRepository extends CrudRepository<Lock, Long> {
    boolean existsByTargetTypeAndTargetId(TargetType targetType, long targetId);

    Optional<Lock> findByTargetTypeAndTargetId(TargetType targetType, long targetId);

    @Modifying
    @Query(value = "DELETE FROM distributed_lock WHERE expires_at < now()", nativeQuery = true)
    void deleteAllExpiredLocks();

    void deleteAllByInstanceUUID(UUID instanceUUID);

    void deleteByTargetTypeAndTargetId(TargetType targetType, long targetId);
}