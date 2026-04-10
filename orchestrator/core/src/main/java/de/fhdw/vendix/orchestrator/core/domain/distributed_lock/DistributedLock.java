package de.fhdw.vendix.orchestrator.core.domain.distributed_lock;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class DistributedLock extends AbstractSpringDataAuditingEntity<Long> {

    @Column(nullable = false, updatable = false)
    @Embedded
    @NotNull(message = "Target cannot be null")
    @Valid
    private EntityTarget target;

    @Column(nullable = false, updatable = false)
    @NotNull(message = "Instance UUID cannot be null")
    private UUID instanceUUID;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @NotNull(message = "Acquired at cannot be null")
    private Instant acquiredAt;

    @Column(nullable = false)
    @NotNull(message = "Expires at cannot be null")
    @Future(message = "Expires at must be in the future")
    private Instant expiresAt;

    protected DistributedLock() {}

    protected DistributedLock(EntityTarget target, UUID instanceUUID, Instant acquiredAt, Instant expiresAt) {
        this.target = target;
        this.instanceUUID = instanceUUID;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }

    @Default
    protected DistributedLock(@Nullable Long id, EntityTarget target, UUID instanceUUID, Instant acquiredAt, Instant expiresAt) {
        super(id);
        this.target = target;
        this.instanceUUID = instanceUUID;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }

    public EntityTarget getTarget() {
        return target;
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