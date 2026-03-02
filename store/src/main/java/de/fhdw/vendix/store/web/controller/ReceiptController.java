package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptEndpoints;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptCommandPort;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ReceiptEndpoints.BASE)
@Tag(name = "Receipt", description = "Endpoints for operations related to receipts")
public class ReceiptController {

    private final ReceiptCommandPort receiptCommandPort;

    public ReceiptController(ReceiptCommandPort receiptCommandPort) {
        this.receiptCommandPort = receiptCommandPort;
    }

    @PostMapping
    public ResponseEntity<ReceiptDTO> createReceipt(@RequestBody ReceiptRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(receiptCommandPort.create(dto));
    }

// TODO: das sollten wir von Beleg trennen. Seperate Entität -> Voucher ?

//    @PostMapping("/redeem/{depositRedemptionCode}")
//    public ResponseEntity<ReceiptDTO> redeemDepositReceipt(@PathVariable String depositRedemptionCode) {
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(receiptMapper
//                        .toDto(receiptService.redeemDepositReceipt(depositRedemptionCode)));
//    }
}
