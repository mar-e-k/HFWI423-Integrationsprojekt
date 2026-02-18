package de.fhdw.vendix.commons.core.api.dto;

import de.fhdw.vendix.commons.core.persistence.entity.LockTypeEnum;

import java.time.Instant;

public class DistributedLockDTO extends AbstractDTO<Long> {

    private LockTypeEnum lockType;
    private Long targetId;
    private String ownerInstance;
    private Instant acquiredAt;
    private Instant expiresAt;

    public DistributedLockDTO() {
        super();
    }

    public DistributedLockDTO(Long id) {
        super(id);
    }

    public DistributedLockDTO(LockTypeEnum lockType, Long targetId, String ownerInstance, Instant acquiredAt, Instant expiresAt) {
        this.lockType = lockType;
        this.targetId = targetId;
        this.ownerInstance = ownerInstance;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }

    public DistributedLockDTO(Long id, LockTypeEnum lockType, Long targetId, String ownerInstance, Instant acquiredAt, Instant expiresAt) {
        super(id);
        this.lockType = lockType;
        this.targetId = targetId;
        this.ownerInstance = ownerInstance;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }

    public LockTypeEnum getLockType() {
        return lockType;
    }

    public void setLockType(LockTypeEnum lockType) {
        this.lockType = lockType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getOwnerInstance() {
        return ownerInstance;
    }

    public void setOwnerInstance(String ownerInstance) {
        this.ownerInstance = ownerInstance;
    }

    public Instant getAcquiredAt() {
        return acquiredAt;
    }

    public void setAcquiredAt(Instant acquiredAt) {
        this.acquiredAt = acquiredAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}