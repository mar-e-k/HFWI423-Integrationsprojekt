package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.spring.web.server.store.api.ReceiptApi;
import de.fhdw.vendix.commons.spring.web.server.store.model.ReceiptDTO;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptMapper;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
class ReceiptController implements ReceiptApi {

    private final ReceiptService receiptService;
    private final ReceiptMapper receiptMapper;

    ReceiptController(ReceiptService receiptService, ReceiptMapper receiptMapper) {
        this.receiptService = receiptService;
        this.receiptMapper = receiptMapper;
    }

    @Override
    public ResponseEntity<List<ReceiptDTO>> postReceipt(ReceiptDTO receiptDTO) {
        return ReceiptApi.super.postReceipt(receiptDTO);
    }
}