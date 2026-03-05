package de.fhdw.vendix.store.core.domain.receipt_voucher;

import de.fhdw.vendix.commons.api.domain.receipt_voucher.ReceiptVoucherEndpoints;
import de.fhdw.vendix.commons.api.domain.receipt_voucher.port.ReceiptVoucherCommandPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ReceiptVoucherEndpoints.BASE)
class ReceiptVoucherController {

    private final ReceiptVoucherCommandPort receiptVoucherCommandPort;

    public ReceiptVoucherController(ReceiptVoucherCommandPort receiptVoucherCommandPort) {
        this.receiptVoucherCommandPort = receiptVoucherCommandPort;
    }

    @PutMapping(ReceiptVoucherEndpoints.BY_CODE)
    public ResponseEntity<Boolean> redeemDepositReceipt(@PathVariable UUID code) {
        receiptVoucherCommandPort.redeemCode(code);
        return ResponseEntity
                .noContent()
                .build();
    }
}