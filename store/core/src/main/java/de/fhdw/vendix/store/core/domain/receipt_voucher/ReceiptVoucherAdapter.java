package de.fhdw.vendix.store.core.domain.receipt_voucher;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.store.core.domain.receipt_voucher.service.ReceiptVoucherService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class ReceiptVoucherAdapter extends AbstractDtoCrudAdapter<ReceiptVoucher, ReceiptVoucherDTO, Long> implements ReceiptVoucherService {

    private final ReceiptVoucherEntityAdapter receiptVoucherEntityAdapter;
    private final ReceiptVoucherMapper receiptVoucherMapper;

    ReceiptVoucherAdapter(ReceiptVoucherEntityAdapter receiptVoucherEntityAdapter, ReceiptVoucherMapper receiptVoucherMapper) {
        super(receiptVoucherEntityAdapter, receiptVoucherMapper);
        this.receiptVoucherEntityAdapter = receiptVoucherEntityAdapter;
        this.receiptVoucherMapper = receiptVoucherMapper;
    }

    @Override
    public Optional<ReceiptVoucherDTO> findByCode(UUID code) {
        if (code == null) {
            return Optional.empty();
        }
        return receiptVoucherEntityAdapter.findByCode(code).map(receiptVoucherMapper::toDTO);
    }

    @Override
    public void redeemCode(UUID code) {
        if (code == null) {
            throw new IllegalArgumentException("Parameter 'code' cannot be null");
        }
        receiptVoucherEntityAdapter.redeemCode(code);
    }
}