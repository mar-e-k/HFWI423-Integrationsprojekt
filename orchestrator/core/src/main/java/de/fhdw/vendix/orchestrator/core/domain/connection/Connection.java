package de.fhdw.vendix.orchestrator.core.domain.connection;

import de.fhdw.vendix.commons.api.domain.connection.ConnectionState;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import de.fhdw.vendix.orchestrator.core.embeddable.entity_target.EntityTarget;
import de.fhdw.vendix.orchestrator.core.embeddable.instance_details.InstanceDetails;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Connection extends AbstractSpringDataAuditingEntity<Long> {

    @Column(nullable = false, updatable = false, unique = true)
    @Embedded
    @NotNull(message = "Target cannot be null")
    @Valid
    private EntityTarget target;

    @Column(nullable = false, updatable = false, unique = true)
    @Embedded
    @NotNull(message = "Instance cannot be null")
    @Valid
    private InstanceDetails instance;

    @Column(nullable = false, updatable = false)
    @NotNull(message = "Acquired at cannot be null")
    private Instant acquiredAt;

    @Column(nullable = false)
    @NotNull(message = "Heartbeat at cannot be null")
    private Instant heartbeatAt;

    @Column(nullable = false)
    @NotNull(message = "Failed attempts cannot be null")
    @Min(value = 0, message = "Failed attempts must be >= 0")
    private Long failedAttempts;

    @Column(nullable = false)
    @NotNull(message = "State cannot be null")
    @Enumerated(EnumType.STRING)
    private ConnectionState connectionState;

    protected Connection() {}

    public Connection(EntityTarget target, InstanceDetails instance, Instant acquiredAt, Instant heartbeatAt, Long failedAttempts, ConnectionState connectionState) {
        this.target = target;
        this.instance = instance;
        this.acquiredAt = acquiredAt;
        this.heartbeatAt = heartbeatAt;
        this.failedAttempts = failedAttempts;
        this.connectionState = connectionState;
    }

    @Default
    protected Connection(
            @Nullable Long id,
            EntityTarget target,
            InstanceDetails instance,
            Instant acquiredAt,
            Instant heartbeatAt,
            Long failedAttempts,
            ConnectionState connectionState
    ) {
        super(id);
        this.target = target;
        this.instance = instance;
        this.acquiredAt = acquiredAt;
        this.heartbeatAt = heartbeatAt;
        this.failedAttempts = failedAttempts;
        this.connectionState = connectionState;
    }

    public EntityTarget getTarget() {
        return target;
    }

    public InstanceDetails getInstance() {
        return instance;
    }

    public Instant getAcquiredAt() {
        return acquiredAt;
    }

    public Instant getHeartbeatAt() {
        return heartbeatAt;
    }

    public Long getFailedAttempts() {
        return failedAttempts;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * Increments {@code failedAttempts} by one and also sets {@code connectionState} to {@code DOWN},
     * if {@code failedAttempts} exceeds 5 after the increment.
     * @return modified Connection
     */
    public Connection incrementFailedAttempts() {
        failedAttempts++;
        if (failedAttempts > 5) {
            connectionState = ConnectionState.DOWN;
        }
        return this;
    }

    /**
     * Resets {@code failedAttempts} back to {@code 0L}.
     * @return modified Connection
     */
    public Connection resetFailedAttempts() {
        failedAttempts = 0L;
        return this;
    }

    /**
     * Modifies {@code heartbeatAt} to be that of the {@code parameter}.
     * @param heartbeatAt the time at which the heartbeat took place. Has to be {@code NonNull} and also be after {@code acquiredAt}.
     * @return modified Connection
     */
    public Connection withHeartbeatAt(Instant heartbeatAt) {
        if (heartbeatAt == null) {
            throw new IllegalArgumentException("Parameter 'heartbeatAt' cannot be null");
        }
        if (heartbeatAt.isBefore(acquiredAt)) {
            throw new IllegalArgumentException("Parameter 'heartbeatAt' cannot be before parameter 'acquiredAt'");
        }
        this.heartbeatAt = heartbeatAt;
        return this;
    }
}