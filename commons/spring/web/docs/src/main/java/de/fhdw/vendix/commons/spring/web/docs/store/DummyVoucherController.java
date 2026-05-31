package de.fhdw.vendix.commons.spring.web.docs.store;

import de.fhdw.vendix.commons.api.domain.voucher.VoucherRequestDTO;
import de.fhdw.vendix.commons.api.domain.voucher.VoucherResponseDTO;
import de.fhdw.vendix.commons.spring.web.api.store.VoucherApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
class DummyVoucherController implements VoucherApi {

    @Override
    public ResponseEntity<List<VoucherResponseDTO>> getVouchers() {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<VoucherResponseDTO> getVoucherByCode(UUID voucherCode) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<VoucherResponseDTO> createVoucher(VoucherRequestDTO requestDTO) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<VoucherResponseDTO> redeemVoucherByCode(UUID voucherCode) {
        return ResponseEntity.noContent().build();
    }
}