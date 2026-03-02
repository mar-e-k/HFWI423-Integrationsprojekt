package de.fhdw.vendix.store.core.domain.lock;

import de.fhdw.vendix.commons.core.persistence.entity.LockTypeEnum;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DistributedLockRepository extends CrudRepository<DistributedLock, Long> {
    boolean existsByLockTypeAndTargetId(LockTypeEnum lockType, Long targetId);
    Optional<DistributedLock> findByLockTypeAndTargetId(LockTypeEnum lockType, Long targetId);
    @Modifying @Query(value = "DELETE FROM distributed_lock WHERE expires_at < now()", nativeQuery = true)
    Long deleteAllExpiredLocks();
    Long deleteAllByOwnerInstance(String ownerInstance);
    Long deleteByLockTypeAndTargetId(LockTypeEnum lockType, Long targetId);
}