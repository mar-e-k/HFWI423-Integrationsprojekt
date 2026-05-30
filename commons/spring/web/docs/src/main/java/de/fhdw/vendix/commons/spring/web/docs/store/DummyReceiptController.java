package de.fhdw.vendix.commons.spring.web.docs.store;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptResponseDTO;
import de.fhdw.vendix.commons.spring.web.api.store.ReceiptApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class DummyReceiptController implements ReceiptApi {

    @Override
    public ResponseEntity<List<ReceiptResponseDTO>> getReceipts() {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ReceiptResponseDTO> postReceipt(ReceiptRequestDTO receiptRequestDTO) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ReceiptResponseDTO> checkoutReceipt(ReceiptRequestDTO receiptRequestDTO) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ReceiptResponseDTO> printReceipt(Long receiptId) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ReceiptResponseDTO> cancelReceipt(Long receiptId) {
        return ResponseEntity.noContent().build();
    }
}