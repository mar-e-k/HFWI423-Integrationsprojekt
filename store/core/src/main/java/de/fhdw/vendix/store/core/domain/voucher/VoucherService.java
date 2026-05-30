package de.fhdw.vendix.store.core.domain.voucher;

import de.fhdw.vendix.commons.spring.data.persistance.service.CrudService;

import java.util.Optional;
import java.util.UUID;

public interface VoucherService extends CrudService<Voucher, Long> {
    Optional<Voucher> findByVoucherCode(UUID code);

    void redeemCode(UUID code);
}