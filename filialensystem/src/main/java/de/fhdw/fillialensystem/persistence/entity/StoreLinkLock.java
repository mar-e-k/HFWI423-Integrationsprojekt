package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
public class StoreLinkLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(nullable = false, unique = true)
    private Store store;

    @NotNull(message = "Lock acquired at cannot be null")
    private Instant lockAcquiredAt;

    public StoreLinkLock() {
        super();
    }

    public StoreLinkLock(Long id, Store store, Instant lockAcquiredAt) {
        this.id = id;
        this.store = store;
        this.lockAcquiredAt = lockAcquiredAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Instant getLockAcquiredAt() {
        return lockAcquiredAt;
    }

    public void setLockAcquiredAt(Instant lockAcquiredAt) {
        this.lockAcquiredAt = lockAcquiredAt;
    }
}
