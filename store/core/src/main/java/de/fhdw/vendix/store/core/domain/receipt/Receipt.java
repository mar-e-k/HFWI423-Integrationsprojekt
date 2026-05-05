package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptPaymentMethod;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptStatus;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

@Entity
public class Receipt extends AbstractSpringDataAuditingEntity<Long> {

    @NotNull(message = "Store ID cannot be null")
    @Min(value = 1, message = "Store ID must be at least 1")
    @Column(nullable = false)
    private Long storeId;

    @NotNull(message = "Register ID cannot be null")
    @Min(value = 1, message = "Register ID must be at least 1")
    @Column(nullable = false)
    private Long registerId;

    @NotNull(message = "Cashier ID cannot be null")
    @Min(value = 1, message = "Cashier ID must be at least 1")
    @Column(nullable = false)
    private Long cashierId;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Payment method cannot be null")
    @Column(nullable = false)
    private ReceiptPaymentMethod receiptPaymentMethod;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status cannot be null")
    @Column(nullable = false)
    private ReceiptStatus status = ReceiptStatus.OPEN;

    protected Receipt() {}

    public Receipt(Long storeId, Long registerId, Long cashierId, ReceiptPaymentMethod receiptPaymentMethod) {
        this.storeId        = storeId;
        this.registerId     = registerId;
        this.cashierId      = cashierId;
        this.receiptPaymentMethod = receiptPaymentMethod;
        this.status         = ReceiptStatus.OPEN;
    }

    @Default
    protected Receipt(@Nullable Long id, Long storeId, Long registerId,
                      Long cashierId, ReceiptPaymentMethod receiptPaymentMethod, ReceiptStatus status) {
        super(id);
        this.storeId       = storeId;
        this.registerId    = registerId;
        this.cashierId     = cashierId;
        this.receiptPaymentMethod = receiptPaymentMethod;
        this.status        = status != null ? status : ReceiptStatus.OPEN;
    }

    // ─── Domain-Methoden ────────────────────────────────────────────────────────

    /**
     * Druckt den Bon ab (Kassenabschluss).
     * Nur möglich wenn Status OPEN ist.
     */
    public Receipt print() throws ReceiptAlreadyPrintedException, ReceiptAlreadyCancelledException {
        if (status == ReceiptStatus.PRINTED) {
            throw new ReceiptAlreadyPrintedException(
                    "Bon " + getId() + " wurde bereits gedruckt und kann nicht nochmal gedruckt werden."
            );
        }
        if (status == ReceiptStatus.CANCELLED) {
            throw new ReceiptAlreadyCancelledException(
                    "Bon " + getId() + " wurde storniert und kann nicht gedruckt werden."
            );
        }
        this.status = ReceiptStatus.PRINTED;
        return this;
    }

    /**
     * Storniert den Bon.
     * Nur möglich wenn Status OPEN ist.
     */
    public Receipt cancel() throws ReceiptAlreadyCancelledException, ReceiptAlreadyPrintedException {
        if (status == ReceiptStatus.CANCELLED) {
            throw new ReceiptAlreadyCancelledException(
                    "Bon " + getId() + " wurde bereits storniert."
            );
        }
        if (status == ReceiptStatus.PRINTED) {
            throw new ReceiptAlreadyPrintedException(
                    "Bon " + getId() + " wurde bereits gedruckt und kann nicht mehr storniert werden."
            );
        }
        this.status = ReceiptStatus.CANCELLED;
        return this;
    }

    // ─── Getter ──────────────────────────────────────────────────────────────────

    public Long getStoreId()              { return storeId; }
    public Long getRegisterId()           { return registerId; }
    public Long getCashierId()            { return cashierId; }
    public ReceiptPaymentMethod getPaymentMethod() { return receiptPaymentMethod; }
    public ReceiptStatus getStatus()      { return status; }
}