package de.fhdw.vendix.commons.spring.data.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class AbstractSpringDataAuditingEntity<ID> extends AbstractSpringDataVersioningEntity<ID> {

    @Column(updatable = false)
    @CreatedDate
    private @Nullable Instant createdAt;

    @Column(updatable = false)
    @CreatedBy
    private @Nullable String createdBy;

    @LastModifiedDate
    private @Nullable Instant changedAt;

    @LastModifiedBy
    private @Nullable String changedBy;

    protected AbstractSpringDataAuditingEntity() {}

    protected AbstractSpringDataAuditingEntity(ID id) {
        super(id);
    }

    public @Nullable Instant getCreatedAt() {
        return createdAt;
    }

    public @Nullable String getCreatedBy() {
        return createdBy;
    }

    public @Nullable Instant getChangedAt() {
        return changedAt;
    }

    public @Nullable String getChangedBy() {
        return changedBy;
    }
}
