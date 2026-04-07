package de.fhdw.vendix.store.core.domain.receipt_voucher;

import de.fhdw.vendix.commons.api.structure.service.CrudService;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptVoucherService extends CrudService<ReceiptVoucher, Long> {
    Optional<ReceiptVoucher> findByCode(UUID code);

    void redeemCode(UUID code);
}