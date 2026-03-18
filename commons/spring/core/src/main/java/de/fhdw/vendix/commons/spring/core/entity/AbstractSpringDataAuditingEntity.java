package de.fhdw.vendix.commons.spring.core.entity;

import jakarta.persistence.*;
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
    private Instant createdAt;

    @Column(updatable = false)
    @CreatedBy
    private String createdBy;

    @LastModifiedDate
    private Instant changedAt;

    @LastModifiedBy
    private String changedBy;

    protected AbstractSpringDataAuditingEntity() {}

    protected AbstractSpringDataAuditingEntity(ID id) {
        super(id);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }
}