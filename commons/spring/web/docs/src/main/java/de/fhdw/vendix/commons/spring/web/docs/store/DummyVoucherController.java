package de.fhdw.vendix.commons.spring.web.docs.store;

import de.fhdw.vendix.commons.api.domain.voucher.VoucherDTO;
import de.fhdw.vendix.commons.spring.web.api.store.VoucherApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class DummyVoucherController implements VoucherApi {

    @Override
    public ResponseEntity<VoucherDTO> getVoucherByCode(UUID code) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<VoucherDTO> redeemVoucherByCode(UUID code) {
        return ResponseEntity.noContent().build();
    }
}