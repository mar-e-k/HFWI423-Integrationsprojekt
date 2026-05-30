package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.PaymentMethod;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptStatus;
import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.persistance.entity.AbstractSpringDataAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Entity
@Table(indexes = {
        @Index(name = "idx_receipt_store_created", columnList = "store_id, created_at"),
        @Index(name = "idx_receipt_register_created", columnList = "register_id, created_at")
})
public class Receipt extends AbstractSpringDataAuditingEntity<Long> {

    @NotNull(message = "Store ID cannot be null")
    @Min(value = 1, message = "Store ID must be at least 1")
    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @NotNull(message = "Register ID cannot be null")
    @Min(value = 1, message = "Register ID must be at least 1")
    @Column(name = "register_id", nullable = false)
    private Long registerId;

    @NotNull(message = "Cashier ID cannot be null")
    @Column(name = "cashier_uuid", nullable = false)
    private UUID cashierUuid;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Payment method cannot be null")
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status cannot be null")
    @Column(nullable = false)
    private ReceiptStatus status;

    protected Receipt() {}

    public Receipt(Long storeId, Long registerId, UUID cashierUuid, PaymentMethod paymentMethod, ReceiptStatus status) {
        this.storeId = storeId;
        this.registerId = registerId;
        this.cashierUuid = cashierUuid;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    @Default
    public Receipt(
            @Nullable Long id,
            Long storeId,
            Long registerId,
            UUID cashierUuid,
            PaymentMethod paymentMethod,
            ReceiptStatus status
    ) {
        super(id);
        this.storeId = storeId;
        this.registerId = registerId;
        this.cashierUuid = cashierUuid;
        this.paymentMethod = paymentMethod;
        this.status = status != null ? status : ReceiptStatus.OPEN;
    }

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

    public Long getStoreId() {
        return storeId;
    }

    public Long getRegisterId() {
        return registerId;
    }

    public UUID getCashierUuid() {
        return cashierUuid;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public ReceiptStatus getStatus() {
        return status;
    }
}