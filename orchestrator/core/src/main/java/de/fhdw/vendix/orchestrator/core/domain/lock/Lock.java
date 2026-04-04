package de.fhdw.vendix.orchestrator.core.domain.lock;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Lock extends AbstractSpringDataAuditingEntity<Long> {

    @Column(nullable = false, updatable = false)
    @Embedded
    private EntityTarget target;

    @Column(nullable = false, updatable = false, name = "instance_uuid")
    private UUID instanceUUID;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant acquiredAt;

    @Column(nullable = false)
    @Future
    private Instant expiresAt;

    protected Lock() {}

    protected Lock(EntityTarget target, UUID instanceUUID, Instant acquiredAt, Instant expiresAt) {
        this.target = target;
        this.instanceUUID = instanceUUID;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }

    @Default
    protected Lock(@Nullable Long id, EntityTarget target, UUID instanceUUID, Instant acquiredAt, Instant expiresAt) {
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