package de.fhdw.vendix.store.core.domain.receipt_voucher;

import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import org.springframework.stereotype.Service;

@Service
public class ReceiptVoucherService extends AbstractSpringDataCrudLogAdapter<ReceiptVoucher, Long> {

    private final ReceiptVoucherRepository receiptVoucherRepository;
    private final ReceiptVoucherMapper receiptVoucherMapper;

    public ReceiptVoucherService(ReceiptVoucherRepository receiptVoucherRepository, ReceiptVoucherMapper receiptVoucherMapper) {
        super(receiptVoucherRepository);
        this.receiptVoucherRepository = receiptVoucherRepository;
        this.receiptVoucherMapper = receiptVoucherMapper;
    }
}