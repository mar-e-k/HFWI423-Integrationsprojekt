package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.DistributedLock;
import de.fhdw.commons.persistence.entity.LockTypeEnum;
import de.fhdw.fillialensystem.persistence.repository.DistributedLockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DistributedLockService extends AbstractCrudService<DistributedLock, Long> {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockService.class);

    public DistributedLockService(DistributedLockRepository distributedLockRepository) {
        super(distributedLockRepository);
    }

    public boolean existsByLockTypeAndTargetId(LockTypeEnum lockType, Long targetId) {
        return ((DistributedLockRepository) repository).existsByLockTypeAndTargetId(lockType, targetId);
    }

    public Optional<DistributedLock> findByLockTypeAndTargetId(LockTypeEnum lockType, Long targetId) {
        return ((DistributedLockRepository) repository).findByLockTypeAndTargetId(lockType, targetId);
    }

    @Transactional
    public Long deleteAllExpiredLocks() {
        Long deletedLockAmount = ((DistributedLockRepository) repository).deleteAllExpiredLocks();
        if (deletedLockAmount > 0) {
            log.atInfo().log("[DELETED] [{}] with lock amount [{}]", this.getClass().getSimpleName(), deletedLockAmount);
        } else {
            log.atInfo().log("[DELETED] [{}] has no locks to delete", this.getClass().getSimpleName());
        }
        return deletedLockAmount;
    }

    @Transactional
    public Long deleteAllByOwnerInstance(String ownerInstance) {
        Long deletedLockAmount = ((DistributedLockRepository) repository).deleteAllByOwnerInstance(ownerInstance);
        if (deletedLockAmount > 0) {
            log.atInfo().log("[DELETED] [{}] from owner instance [{}] with lock amount [{}]", this.getClass().getSimpleName(), ownerInstance, deletedLockAmount);
        } else {
            log.atInfo().log("[DELETED] [{}] from owner instance [{}] has no locks to delete", this.getClass().getSimpleName(), ownerInstance);
        }
        return deletedLockAmount;
    }


    @Transactional
    public Long deleteByLockTypeAndTargetId(LockTypeEnum lockType, Long targetId) {
        Long deletedLockAmount = ((DistributedLockRepository) repository).deleteByLockTypeAndTargetId(lockType, targetId);
        if (deletedLockAmount > 0) {
            log.atInfo().log("[DELETED] [{}] with lockType [{}] and id [{}] with lock amount [{}]", this.getClass().getSimpleName(), lockType.name(), targetId, deletedLockAmount);
        } else {
            log.atInfo().log("[DELETED] [{}] with lockType [{}] and id [{}] has no locks to delete", this.getClass().getSimpleName(), lockType.name(), targetId);
        }
        return deletedLockAmount;
    }
}