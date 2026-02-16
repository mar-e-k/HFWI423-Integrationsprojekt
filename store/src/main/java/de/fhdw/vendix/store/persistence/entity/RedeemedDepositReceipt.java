package de.fhdw.vendix.store.persistence.entity;

import de.fhdw.vendix.commons.core.persistence.entity.GenericEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// Change this in Lock Table

@Entity
@Table(name = "redeemed_deposit_receipt")
public class RedeemedDepositReceipt implements GenericEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    @NotNull
    @Column(name = "redeemed_timestamp", nullable = false)
    private LocalDateTime redeemedTimestamp;

    public RedeemedDepositReceipt() {
        super();
    }

    public RedeemedDepositReceipt(Receipt receipt, LocalDateTime redeemedTimestamp) {
        this.receipt = receipt;
        this.redeemedTimestamp = redeemedTimestamp;
    }

    public RedeemedDepositReceipt(Long id, Receipt receipt, LocalDateTime redeemedTimestamp) {
        this.id = id;
        this.receipt = receipt;
        this.redeemedTimestamp = redeemedTimestamp;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public LocalDateTime getRedeemedTimestamp() {
        return redeemedTimestamp;
    }

    public void setRedeemedTimestamp(LocalDateTime redeemedTimestamp) {
        this.redeemedTimestamp = redeemedTimestamp;
    }
}
