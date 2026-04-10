package de.fhdw.vendix.commons.api.domain.distributed_lock;

import de.fhdw.vendix.commons.api.structure.dto.ResponseDTO;

public record DistributedLockResponseDTO() implements ResponseDTO {
    public DistributedLockResponseDTO() {
        throw new UnsupportedOperationException("LockResponseDTO has not yet been implemented");
    }
}