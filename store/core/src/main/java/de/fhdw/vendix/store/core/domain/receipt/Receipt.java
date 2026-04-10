package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    protected Receipt() {}

    public Receipt(Long storeId, Long registerId, Long cashierId) {
        this.storeId = storeId;
        this.registerId = registerId;
        this.cashierId = cashierId;
    }

    @Default
    protected Receipt(@Nullable Long id, Long storeId, Long registerId, Long cashierId) {
        super(id);
        this.storeId = storeId;
        this.registerId = registerId;
        this.cashierId = cashierId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public Long getRegisterId() {
        return registerId;
    }

    public Long getCashierId() {
        return cashierId;
    }
}