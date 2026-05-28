package de.fhdw.vendix.commons.spring.web.docs.store;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.spring.web.api.store.ReceiptApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DummyReceiptController implements ReceiptApi {

    @Override
    public ResponseEntity<ReceiptDTO> postReceipt(ReceiptDTO receiptDTO) {
        return ResponseEntity.noContent().build();
    }
}