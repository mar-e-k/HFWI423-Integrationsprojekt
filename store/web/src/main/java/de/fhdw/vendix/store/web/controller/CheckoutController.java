package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.checkout.CheckoutRequestDTO;
import de.fhdw.vendix.commons.api.domain.checkout.CheckoutResponseDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.spring.web.api.store.CheckoutApi;
import de.fhdw.vendix.store.core.domain.receipt.CheckoutService;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptAlreadyCancelledException;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptAlreadyPrintedException;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptMapper;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CheckoutController implements CheckoutApi {

    private final CheckoutService checkoutService;
    private final ReceiptService  receiptService;
    private final ReceiptMapper   receiptMapper;

    CheckoutController(CheckoutService checkoutService,
                       ReceiptService receiptService,
                       ReceiptMapper receiptMapper) {
        this.checkoutService = checkoutService;
        this.receiptService  = receiptService;
        this.receiptMapper   = receiptMapper;
    }

    @Override
    public ResponseEntity<CheckoutResponseDTO> checkout(CheckoutRequestDTO request) {
        CheckoutResponseDTO response = checkoutService.checkout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ReceiptDTO> printReceipt(Long id) {
        try {
            return ResponseEntity.ok(receiptMapper.toDTO(receiptService.printReceipt(id)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ReceiptAlreadyPrintedException | ReceiptAlreadyCancelledException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Override
    public ResponseEntity<ReceiptDTO> cancelReceipt(Long id) {
        try {
            return ResponseEntity.ok(receiptMapper.toDTO(receiptService.cancelReceipt(id)));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ReceiptAlreadyCancelledException | ReceiptAlreadyPrintedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}