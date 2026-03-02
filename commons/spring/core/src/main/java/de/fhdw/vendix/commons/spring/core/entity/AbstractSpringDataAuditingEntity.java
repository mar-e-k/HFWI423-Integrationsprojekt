package de.fhdw.vendix.commons.spring.core.entity;

import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractSpringDataAuditingEntity<ID> extends AbstractSpringDataVersioningEntity<ID> {

    public AbstractSpringDataAuditingEntity() {}

    public AbstractSpringDataAuditingEntity(@Nullable Instant createdAt, @Nullable String createdBy, @Nullable Instant changedAt, @Nullable String changedBy) {
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.changedAt = changedAt;
        this.changedBy = changedBy;
    }

    @CreatedDate
    @Column(updatable = false)
    private @Nullable Instant createdAt;

    @CreatedBy
    @Column(updatable = false)
    private @Nullable String createdBy;

    @LastModifiedDate
    private @Nullable Instant changedAt;

    @LastModifiedBy
    private @Nullable String changedBy;

    public @Nullable Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@Nullable Instant createdAt) {
        this.createdAt = createdAt;
    }

    public @Nullable String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(@Nullable String createdBy) {
        this.createdBy = createdBy;
    }

    public @Nullable Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(@Nullable Instant changedAt) {
        this.changedAt = changedAt;
    }

    public @Nullable String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(@Nullable String changedBy) {
        this.changedBy = changedBy;
    }
}