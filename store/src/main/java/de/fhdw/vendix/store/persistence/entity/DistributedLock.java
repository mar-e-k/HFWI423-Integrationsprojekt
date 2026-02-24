package de.fhdw.vendix.store.persistence.entity;

import de.fhdw.vendix.commons.core.persistence.entity.GenericEntity;
import de.fhdw.vendix.commons.core.persistence.entity.LockTypeEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

// TODO: this should really be in redis

@Entity
@Table(
        indexes = @Index(columnList = "expires_at"),
        uniqueConstraints = {@UniqueConstraint(columnNames = {"lock_type", "target_id"})}
)
@EntityListeners(AuditingEntityListener.class)
public class DistributedLock implements GenericEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Lock type cannot be null")
    private LockTypeEnum lockType;

    @Column(nullable = false)
    @NotNull(message = "Target id cannot be null")
    private Long targetId;

    @Column(nullable = false)
    @NotBlank(message = "Owner instance must not be blank")
    private String ownerInstance;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant acquiredAt;

    @Column(nullable = false)
    @Future(message = "Expires at must be set to a future date/time")
    @NotNull(message = "Expires at cannot be null")
    private Instant expiresAt;

    public DistributedLock() {
        super();
    }

    public DistributedLock(LockTypeEnum lockType, Long targetId, String ownerInstance, Instant expiresAt) {
        this.lockType = lockType;
        this.targetId = targetId;
        this.ownerInstance = ownerInstance;
        this.expiresAt = expiresAt;
    }

    public DistributedLock(Long id, LockTypeEnum lockType, Long targetId, String ownerInstance, Instant expiresAt) {
        this.id = id;
        this.lockType = lockType;
        this.targetId = targetId;
        this.ownerInstance = ownerInstance;
        this.expiresAt = expiresAt;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
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