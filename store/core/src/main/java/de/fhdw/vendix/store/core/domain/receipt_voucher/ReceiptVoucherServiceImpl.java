package de.fhdw.vendix.store.core.domain.receipt_voucher;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
class ReceiptVoucherServiceImpl extends AbstractEntityCrudAdapter<ReceiptVoucher, Long> implements ReceiptVoucherService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptVoucherServiceImpl.class);

    private final ReceiptVoucherRepository receiptVoucherRepository;

    ReceiptVoucherServiceImpl(ReceiptVoucherRepository receiptVoucherRepository) {
        super(receiptVoucherRepository);
        this.receiptVoucherRepository = receiptVoucherRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReceiptVoucher> findByCode(UUID code) {
        if (code == null) {
            return Optional.empty();
        }
        return receiptVoucherRepository.findByCode(code);
    }

    @Override
    @Transactional
    public void redeemCode(UUID code) {
        if (code == null) {
            throw new IllegalArgumentException("Parameter 'code' cannot be null");
        }
        ReceiptVoucher receiptVoucher = findByCode(code)
                .orElseThrow(EntityNotFoundException::new);
        ReceiptVoucher redeemed = receiptVoucher.redeem();
        log.atInfo().log("Redeeming receipt voucher with code: {}", code);
        super.update(redeemed);
        log.atInfo().log("Successfully redeemed voucher with code: {}", code);
    }
}