package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.spring.web.server.store.api.ReceiptApi;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptMapper;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ReceiptController implements ReceiptApi {

    private final ReceiptService receiptService;
    private final ReceiptMapper receiptMapper;

    ReceiptController(ReceiptService receiptService, ReceiptMapper receiptMapper) {
        this.receiptService = receiptService;
        this.receiptMapper = receiptMapper;
    }

    @Override
    public ResponseEntity<ReceiptDTO> postReceipt(ReceiptDTO receiptDTO) {
        Receipt receipt = receiptMapper.toEntity(receiptDTO);
        Receipt created = receiptService.create(receipt);
        ReceiptDTO dto = receiptMapper.toDTO(created);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(dto);
    }
}