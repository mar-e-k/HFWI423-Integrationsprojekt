package de.fhdw.vendix.commons.api.domain.receipt.dto;

import de.fhdw.vendix.commons.api.domain.account.dto.AccountDTO;
import de.fhdw.vendix.commons.api.domain.register.dto.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

public record ReceiptDTO (
        @Nullable Long id,
        StoreDTO store,
        RegisterDTO register,
        AccountDTO cashier
) implements DomainDTO {
    public ReceiptDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'id' must be at least 0");
        }
        if (store == null) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'store' must not be null");
        }
        if (register == null) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'register' must not be null");
        }
        if (cashier == null) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'cashier' must not be null");
        }
    }
}