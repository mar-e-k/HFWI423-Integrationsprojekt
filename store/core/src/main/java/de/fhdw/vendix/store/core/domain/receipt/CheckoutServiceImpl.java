package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.CheckoutLineDTO;
import de.fhdw.vendix.commons.api.domain.receipt.CheckoutRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.CheckoutResponseDTO;
import de.fhdw.vendix.commons.api.embeddable.DiscountOverrideDTO;
import de.fhdw.vendix.commons.api.embeddable.OverrideReason;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLineMapper;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLineService;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
class CheckoutServiceImpl implements CheckoutService {

    private final ReceiptService     receiptService;
    private final ReceiptLineService receiptLineService;
    private final ReceiptLineMapper  receiptLineMapper;

    CheckoutServiceImpl(ReceiptService receiptService,
                        ReceiptLineService receiptLineService,
                        ReceiptLineMapper receiptLineMapper) {
        this.receiptService     = receiptService;
        this.receiptLineService = receiptLineService;
        this.receiptLineMapper  = receiptLineMapper;
    }

    @Override
    @Transactional
    public CheckoutResponseDTO checkout(CheckoutRequestDTO request) {

        // 1. Bon-Header erstellen
        Receipt receipt = new Receipt(
                request.storeId(),
                request.registerId(),
                request.cashierId(),
                request.paymentMethod()
        );
        Long receiptId = Objects.requireNonNull(
                receiptService.create(receipt).getId(),
                "Receipt ID nach save() ist null — Hibernate-Fehler"
        );

        // 2. Bon-Positionen speichern
        List<Long> lineIds = new ArrayList<>();

        for (CheckoutLineDTO line : request.lines()) {

            DiscountOverrideDTO discount = null;
            if (line.discountPercent() != null) {
                discount = new DiscountOverrideDTO(
                        line.discountPercent(),
                        OverrideReason.PROMOTIONAL_ADJUSTMENT
                );
            }

            ReceiptLine savedLine = receiptLineService.create(
                    receiptLineMapper.toEntity(new ReceiptLineDTO(
                            null,
                            receiptId,
                            line.articleId(),
                            line.articleAmount(),
                            discount,
                            null
                    ))
            );
            lineIds.add(savedLine.getId());
        }

        // 3. Response
        return new CheckoutResponseDTO(
                receiptId,
                request.storeId(),
                request.registerId(),
                request.cashierId(),
                lineIds.size(),
                lineIds
        );
    }
}