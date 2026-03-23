package de.fhdw.vendix.store.core.persistance.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class LockAdapter extends AbstractDtoCrudAdapter<Lock, LockDTO, Long> implements LockQueryPort, LockCommandPort {

    private static final Logger log = LoggerFactory.getLogger(LockAdapter.class);
    private final LockEntityAdapter lockEntityAdapter;
    private final LockMapper lockMapper;

    LockAdapter(LockEntityAdapter lockEntityAdapter, LockMapper lockMapper) {
        super(lockEntityAdapter, lockMapper);
        this.lockEntityAdapter = lockEntityAdapter;
        this.lockMapper = lockMapper;
    }

    @Override
    public Optional<LockDTO> findByTargetTypeAndTargetID(TargetTypeEnum targetTypeEnum, Long targetId) {
        if (targetTypeEnum == null) {
            return Optional.empty();
        }
        if (targetId < 0) {
            return Optional.empty();
        }
        return lockEntityAdapter.findByTargetTypeAndTargetId(targetTypeEnum, targetId)
                .map(lockMapper::toDTO);
    }

    @Override
    public boolean existsByTargetTypeAndTargetID(TargetTypeEnum targetTypeEnum, Long targetId) {
        if (targetTypeEnum == null) {
            return false;
        }
        if (targetId < 0) {
            return false;
        }
        return lockEntityAdapter.existsByTargetTypeAndTargetId(targetTypeEnum, targetId);
    }

    @Override
    public void deleteAllByExpiresAtNow() {
        log.atInfo().log("Deleting all expired locks");
        lockEntityAdapter.deleteAllByExpiresAtNow();
        log.atInfo().log("Successfully deleted all expired locks");
    }

    @Override
    public void deleteAllByInstanceUUID(UUID instanceUUID) {
        log.atInfo().log("Deleting all locks by instance");
        if (instanceUUID == null) {
            throw new IllegalArgumentException("Parameter 'instanceUUID' cannot be null");
        }
        lockEntityAdapter.deleteAllByInstanceUUID(instanceUUID);
        log.atInfo().log("Successfully deleted all locks by instance");
    }

    @Override
    public void deleteByTargetTypeAndTargetId(TargetTypeEnum targetTypeEnum, long targetId) {
        log.atInfo().log("Deleting all locks by target type and target id");
        if (targetTypeEnum == null) {
            throw new IllegalArgumentException("Parameter 'targetType' cannot be null");
        }
        if (targetId < 0) {
            throw new IllegalArgumentException("Parameter 'targetId' cannot be negative");
        }
        lockEntityAdapter.deleteAllByTargetTypeAndTargetId(targetTypeEnum, targetId);
        log.atInfo().log("Successfully deleted all locks by target type and target id");
    }
}