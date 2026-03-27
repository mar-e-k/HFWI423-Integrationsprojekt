package de.fhdw.vendix.store.core.persistance.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
class LockEntityAdapter extends AbstractEntityCrudAdapter<Lock, Long> {

    private static final Logger log = LoggerFactory.getLogger(LockEntityAdapter.class);

    private final LockRepository lockRepository;

    LockEntityAdapter(LockRepository lockRepository) {
        super(lockRepository);
        this.lockRepository = lockRepository;
    }

    @Transactional(readOnly = true)
    public boolean existsByTargetTypeAndTargetId(TargetTypeEnum targetTypeEnum, long targetId) {
        if (targetTypeEnum == null) {
            return false;
        }
        if (targetId < 0L) {
            return false;
        }
        return lockRepository.existsByTargetTypeAndTargetId(targetTypeEnum, targetId);
    }

    @Transactional(readOnly = true)
    public Optional<Lock> findByTargetTypeAndTargetId(TargetTypeEnum targetTypeEnum, long targetId) {
        if (targetTypeEnum == null) {
            return Optional.empty();
        }
        if (targetId < 0L) {
            return Optional.empty();
        }
        return lockRepository.findByTargetTypeAndTargetId(targetTypeEnum, targetId);
    }

    @Transactional(readOnly = true)
    public Set<Lock> findAllByTargetType(TargetTypeEnum targetType) {
        if (targetType == null) {
            return Set.of();
        }
        return lockRepository.findAllByTargetType(targetType);
    }

    @Transactional(readOnly = true)
    public Set<Lock> findAllByTargetId(long targetId) {
        if (targetId < 0L) {
            return Set.of();
        }
        return lockRepository.findAllByTargetId(targetId);
    }

    @Transactional(readOnly = true)
    public Set<Lock> findAllByInstanceUUID(UUID instanceUUID) {
        if (instanceUUID == null) {
            return Set.of();
        }
        return lockRepository.findAllByInstanceUUID(instanceUUID);
    }

    @Transactional
    public void deleteAllByExpiresAtNow() {
        log.atInfo().log("[DELETE] Deleting all expired locks");
        lockRepository.deleteAllByExpiresAtNow();
        log.atInfo().log("[DELETE] Successfully deleted all expired locks");
    }

    @Transactional
    public void deleteAllByInstanceUUID(UUID instanceUUID) {
        log.atInfo().log("[DELETE] Deleting all locks by instance");
        if (instanceUUID == null) {
            throw new IllegalArgumentException("Parameter 'instanceUUID' cannot be null");
        }
        lockRepository.deleteAllByInstanceUUID(instanceUUID);
        log.atInfo().log("[DELETE] Successfully deleted all locks by instance");
    }

    @Transactional
    public void deleteAllByTargetTypeAndTargetId(TargetTypeEnum targetType, long targetId) {
        log.atInfo().log("[DELETE] Deleting all locks by target type and target id");
        if (targetType == null) {
            throw new IllegalArgumentException("Parameter 'targetType' cannot be null");
        }
        if (targetId < 0L) {
            throw new IllegalArgumentException("Parameter 'targetId' cannot be negative");
        }
        lockRepository.deleteAllByTargetTypeAndTargetId(targetType, targetId);
        log.atInfo().log("[DELETE] Successfully deleted all locks by target type and target id");
    }
}