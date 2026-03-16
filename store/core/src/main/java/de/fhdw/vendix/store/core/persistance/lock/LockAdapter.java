package de.fhdw.vendix.store.core.persistance.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.domain.lock.port.LockCommandPort;
import de.fhdw.vendix.commons.api.domain.lock.port.LockQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractCrudLogAdapter;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class LockAdapter extends AbstractCrudLogAdapter<Lock, Long> implements LockCommandPort, LockQueryPort {

    private final LockRepository lockRepository;
    private final LockMapper lockMapper;

    protected LockAdapter(LockRepository lockRepository, LockMapper lockMapper) {
        super(lockRepository);
        this.lockRepository = lockRepository;
        this.lockMapper = lockMapper;
    }

    @Override
    public LockDTO create(LockDTO dto) {
        Lock lock = lockMapper.toEntity(dto);
        lock = super.create(lock);
        return lockMapper.toDTO(lock);
    }

    @Override
    public void deleteByTargetTypeAndTargetId(TargetTypeEnum targetTypeEnum, long targetId) {
        lockRepository.deleteByTargetTypeAndTargetId(targetTypeEnum, targetId);
    }

    @Override
    public void deleteAllByInstanceUUID(UUID instanceUUID) {
        lockRepository.deleteAllByInstanceUUID(instanceUUID);
    }

    @Override
    public void deleteAllExpiredLocks() {
        lockRepository.deleteAllExpiredLocks();
    }

    @Override
    public Optional<LockDTO> findByTargetTypeAndTargetID(TargetTypeEnum targetTypeEnum, long targetID) {
        if (targetTypeEnum == null) {
            throw new IllegalArgumentException("Parameter 'targetType' cannot be null.");
        }
        return lockRepository.findByTargetTypeAndTargetId(targetTypeEnum, targetID)
                .map(lockMapper::toDTO);
    }

    @Override
    public boolean existsByTargetTypeAndTargetID(TargetTypeEnum targetTypeEnum, long id) {
        if (targetTypeEnum == null) {
            throw new IllegalArgumentException("Parameter 'targetType' cannot be null.");
        }
        return lockRepository.existsByTargetTypeAndTargetId(targetTypeEnum, id);
    }
}