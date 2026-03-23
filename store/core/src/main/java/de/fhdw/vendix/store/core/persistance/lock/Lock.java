package de.fhdw.vendix.store.core.persistance.lock;

import de.fhdw.vendix.commons.api.domain.lock.dto.TargetTypeEnum;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

// TODO: this should really be in redis

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"target_type", "target_id"})})
@EntityListeners(AuditingEntityListener.class)
public class Lock extends AbstractSpringDataAuditingEntity<Long> {

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TargetTypeEnum targetType;

    @Column(nullable = false)
    private long targetId;

    @Column(nullable = false)
    private UUID instanceUUID;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant acquiredAt;

    @Column(nullable = false)
    @Future
    private Instant expiresAt;

    protected Lock() {}

    protected Lock(TargetTypeEnum targetType, long targetId, UUID instanceUUID, Instant acquiredAt, Instant expiresAt) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.instanceUUID = instanceUUID;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }

    @Default
    protected Lock(@Nullable Long id, TargetTypeEnum targetType, long targetId, UUID instanceUUID, Instant acquiredAt, Instant expiresAt) {
        super(id);
        this.targetType = targetType;
        this.targetId = targetId;
        this.instanceUUID = instanceUUID;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }

    public TargetTypeEnum getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
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