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

/**
 * Implementierung des Kassenabschluss-Services.
 *
 * Ablauf:
 *  1. Receipt anlegen (storeId, registerId, cashierId, paymentMethod)
 *  2. Für jede Zeile eine ReceiptLine speichern — optional mit Rabatt
 *  3. Alles in einer Transaktion → bei Fehler vollständiger Rollback
 */
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

        // ── 1. Bon-Header erstellen (inkl. PaymentMethod) ──────────────────────
        Receipt receipt = new Receipt(
                request.storeId(),
                request.registerId(),
                request.cashierId(),
                request.paymentMethod()   // neu: PaymentMethod wird jetzt persistiert
        );
        Receipt saved = receiptService.create(receipt);
        Long receiptId = saved.getId();
        if (receiptId == null) {
            throw new IllegalStateException("Bon wurde gespeichert, hat aber keine ID erhalten");
        }

        // ── 2. Bon-Positionen speichern ────────────────────────────────────────
        List<Long> lineIds = new ArrayList<>();

        for (CheckoutLineDTO line : request.lines()) {

            DiscountOverrideDTO discount = null;
            if (line.discountPercent() != null) {
                discount = new DiscountOverrideDTO(
                        line.discountPercent(),
                        OverrideReason.PROMOTIONAL_ADJUSTMENT
                );
            }

            ReceiptLineDTO lineDTO = new ReceiptLineDTO(
                    null,
                    receiptId,
                    line.articleId(),
                    line.articleAmount(),
                    discount,
                    null
            );

            ReceiptLine receiptLine = receiptLineMapper.toEntity(lineDTO);
            ReceiptLine savedLine   = receiptLineService.create(receiptLine);
            lineIds.add(savedLine.getId());
        }

        // ── 3. Response zusammenbauen ──────────────────────────────────────────
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