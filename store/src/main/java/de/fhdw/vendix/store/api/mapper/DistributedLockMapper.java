package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.core.api.dto.DistributedLockDTO;
import de.fhdw.vendix.commons.core.api.mapper.GenericMapper;
import de.fhdw.vendix.store.persistence.entity.DistributedLock;
import org.springframework.stereotype.Component;

@Component
public class DistributedLockMapper implements GenericMapper<DistributedLock, DistributedLockDTO> {

    public DistributedLockMapper() {}

    @Override
    public DistributedLock toEntity(DistributedLockDTO dto) {
        DistributedLock distributedLock = new DistributedLock();
        distributedLock.setId(dto.getId());
        distributedLock.setLockType(dto.getLockType());
        distributedLock.setTargetId(dto.getTargetId());
        distributedLock.setOwnerInstance(dto.getOwnerInstance());
        distributedLock.setAcquiredAt(dto.getAcquiredAt());
        distributedLock.setExpiresAt(dto.getExpiresAt());
        return distributedLock;
    }

    @Override
    public DistributedLockDTO toDto(DistributedLock distributedLock) {
        DistributedLockDTO dto = new DistributedLockDTO();
        dto.setId(distributedLock.getId());
        dto.setLockType(distributedLock.getLockType());
        dto.setTargetId(distributedLock.getTargetId());
        dto.setOwnerInstance(distributedLock.getOwnerInstance());
        dto.setAcquiredAt(distributedLock.getAcquiredAt());
        dto.setExpiresAt(distributedLock.getExpiresAt());
        return dto;
    }
}
