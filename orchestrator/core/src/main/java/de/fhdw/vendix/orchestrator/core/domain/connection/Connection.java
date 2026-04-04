package de.fhdw.vendix.orchestrator.core.domain.connection;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import de.fhdw.vendix.orchestrator.core.embeddable.instance_details.InstanceDetails;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Connection extends AbstractSpringDataAuditingEntity<Long> {

    @Column(nullable = false, updatable = false)
    @Embedded
    private EntityTarget target;

    @Column(nullable = false, updatable = false)
    @Embedded
    private InstanceDetails instance;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant acquiredAt;

    @Column(nullable = false)
    @Future
    private Instant heartbeatAt;

    protected Connection() {}

    protected Connection(EntityTarget target, InstanceDetails instance, Instant acquiredAt, Instant heartbeatAt) {
        this.target = target;
        this.instance = instance;
        this.acquiredAt = acquiredAt;
        this.heartbeatAt = heartbeatAt;
    }

    @Default
    protected Connection(@Nullable Long id, EntityTarget target, InstanceDetails instance, Instant acquiredAt, Instant heartbeatAt) {
        super(id);
        this.target = target;
        this.instance = instance;
        this.acquiredAt = acquiredAt;
        this.heartbeatAt = heartbeatAt;
    }

    public EntityTarget getTarget() {
        return target;
    }

    public InstanceDetails getConnectionDetails() {
        return instance;
    }

    public Instant getAcquiredAt() {
        return acquiredAt;
    }

    public Instant getHeartbeatAt() {
        return heartbeatAt;
    }
}