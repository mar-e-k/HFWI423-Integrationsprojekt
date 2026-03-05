package de.fhdw.vendix.store.core.domain.receipt_voucher;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.dto.ReceiptVoucherDTO;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.port.ReceiptVoucherCommandPort;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.port.ReceiptVoucherQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
class ReceiptVoucherService extends AbstractSpringDataCrudLogAdapter<ReceiptVoucher, Long> implements ReceiptVoucherCommandPort, ReceiptVoucherQueryPort {

    private final ReceiptVoucherRepository receiptVoucherRepository;
    private final ReceiptVoucherMapper receiptVoucherMapper;

    public ReceiptVoucherService(ReceiptVoucherRepository receiptVoucherRepository, ReceiptVoucherMapper receiptVoucherMapper) {
        super(receiptVoucherRepository);
        this.receiptVoucherRepository = receiptVoucherRepository;
        this.receiptVoucherMapper = receiptVoucherMapper;
    }

    @Override
    public Optional<ReceiptVoucherDTO> findByCode(UUID code) {
        return receiptVoucherRepository.findByCode(code)
                .map(receiptVoucherMapper::toDTO);
    }

    @Override
    public void redeemCode(UUID code) {
        receiptVoucherRepository.findByCode(code)
                .map(ReceiptVoucher::redeem)
                .map(super::update)
                .orElseThrow(EntityNotFoundException::new);
    }
}