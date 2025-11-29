package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
@Table(name = "store_lock")
public class StoreLock extends AbstractEntity {

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Store ID must not be blank")
    private String storeId;

    @Column(nullable = false)
    @NotBlank(message = "Instance ID must not be blank")
    private String lockedByInstanceId;

    @Column(nullable = false)
    @NotNull(message = "Lock timestamp must not be null")
    private Instant lockTimestamp;

    public StoreLock() {
        super();
    }

    public StoreLock(String storeId, String lockedByInstanceId, Instant lockTimestamp) {
        this.storeId = storeId;
        this.lockedByInstanceId = lockedByInstanceId;
        this.lockTimestamp = lockTimestamp;
    }

    // Getter and Setter
    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getLockedByInstanceId() {
        return lockedByInstanceId;
    }

    public void setLockedByInstanceId(String lockedByInstanceId) {
        this.lockedByInstanceId = lockedByInstanceId;
    }

    public Instant getLockTimestamp() {
        return lockTimestamp;
    }

    public void setLockTimestamp(Instant lockTimestamp) {
        this.lockTimestamp = lockTimestamp;
    }
}
