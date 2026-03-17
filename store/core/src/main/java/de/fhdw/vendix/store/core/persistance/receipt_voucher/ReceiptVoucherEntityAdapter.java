package de.fhdw.vendix.store.core.persistance.receipt_voucher;

import de.fhdw.vendix.commons.spring.core.crud.AbstractEntityCrudAdapter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
class ReceiptVoucherEntityAdapter extends AbstractEntityCrudAdapter<ReceiptVoucher, Long> {

    private final ReceiptVoucherRepository receiptVoucherRepository;

    ReceiptVoucherEntityAdapter(ReceiptVoucherRepository receiptVoucherRepository) {
        super(receiptVoucherRepository);
        this.receiptVoucherRepository = receiptVoucherRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ReceiptVoucher> findByCode(UUID code) {
        if (code == null) {
            return Optional.empty();
        }
        return receiptVoucherRepository.findByCode(code);
    }

    @Transactional
    public void redeemCode(UUID code) {
        if (code == null) {
            throw new IllegalArgumentException("Parameter 'code' cannot be null");
        }
        ReceiptVoucher receiptVoucher = findByCode(code)
                .orElseThrow(EntityNotFoundException::new);
        receiptVoucher.redeem();
        super.update(receiptVoucher);
    }
}