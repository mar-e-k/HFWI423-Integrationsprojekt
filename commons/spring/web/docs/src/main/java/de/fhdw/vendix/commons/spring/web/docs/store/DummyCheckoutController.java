package de.fhdw.vendix.commons.spring.web.docs.store;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.checkout.CheckoutRequestDTO;
import de.fhdw.vendix.commons.api.domain.checkout.CheckoutResponseDTO;
import de.fhdw.vendix.commons.spring.web.api.store.CheckoutApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DummyCheckoutController implements CheckoutApi {

    @Override
    public ResponseEntity<CheckoutResponseDTO> checkout(CheckoutRequestDTO request) {
        return ResponseEntity.noContent().build();

    }

    @Override
    public ResponseEntity<ReceiptDTO> printReceipt(Long id) {
        return ResponseEntity.noContent().build();

    }

    @Override
    public ResponseEntity<ReceiptDTO> cancelReceipt(Long id) {
        return ResponseEntity.noContent().build();
    }
}