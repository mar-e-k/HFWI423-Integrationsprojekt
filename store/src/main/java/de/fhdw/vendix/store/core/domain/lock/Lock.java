package de.fhdw.vendix.store.core.domain.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.LockRequestDTO;
import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.spring.core.entity.AbstractSpringDataAuditingEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

// TODO: this should really be in redis

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"lock_type", "target_id"})})
@EntityListeners(AuditingEntityListener.class)
class Lock extends AbstractSpringDataAuditingEntity<Long> {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private TargetTypeEnum targetType;

    @Column(nullable = false)
    @NotNull
    private long targetID;

    @Column(nullable = false)
    @NotBlank
    private UUID instanceUUID;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant acquiredAt;

    @Column(nullable = false)
    @Future
    @NotNull
    private Instant expiresAt;

    protected Lock() {}

    protected Lock(TargetTypeEnum targetType, long targetID, UUID instanceUUID, Instant acquiredAt, Instant expiresAt) {
        this.targetType = targetType;
        this.targetID = targetID;
        this.instanceUUID = instanceUUID;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }

    public TargetTypeEnum getTargetType() {
        return targetType;
    }

    public Long getTargetID() {
        return targetID;
    }

    public UUID getInstanceUUID() {
        return instanceUUID;
    }

    public Instant getAcquiredAt() {
        return acquiredAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}