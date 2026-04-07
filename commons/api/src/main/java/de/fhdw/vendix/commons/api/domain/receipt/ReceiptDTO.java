package de.fhdw.vendix.commons.api.domain.receipt;

import de.fhdw.vendix.commons.api.domain.register.RegisterDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

public record ReceiptDTO (
        @Nullable Long id,
        StoreDTO store,
        RegisterDTO register,
        Long cashierId
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
        if (cashierId == null || cashierId < 0) {
            throw new IllegalArgumentException("ReceiptDTO parameter 'cashier' must not be null or negative");
        }
    }
}