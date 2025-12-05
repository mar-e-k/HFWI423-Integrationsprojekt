package de.fhdw.fillialensystem.persistence.entity;

import de.fhdw.commons.persistence.entity.GenericEntity;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@MappedSuperclass
public abstract class AbstractLock implements GenericEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    private Instant lockAcquiredAt;

    public AbstractLock() {
        super();
    }

    public AbstractLock(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Instant getLockAcquiredAt() {
        return lockAcquiredAt;
    }

    public void setLockAcquiredAt(Instant lockAcquiredAt) {
        this.lockAcquiredAt = lockAcquiredAt;
    }
}
