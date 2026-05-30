package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.store.core.domain.receipt.ReceiptAlreadyCancelledException;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptAlreadyCheckedOutException;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptAlreadyPrintedException;
import de.fhdw.vendix.store.core.domain.voucher.VoucherAlreadyRedeemedException;
import de.fhdw.vendix.store.core.domain.voucher.VoucherExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class StoreExceptionHandler {

    @ExceptionHandler({
            VoucherExpiredException.class,
            VoucherAlreadyRedeemedException.class
    })
    ResponseEntity<Void> handleVoucherExceptions() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler({
            ReceiptAlreadyCheckedOutException.class,
            ReceiptAlreadyCancelledException.class,
            ReceiptAlreadyPrintedException.class
    })
    ResponseEntity<Void> handleReceiptExceptions() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}