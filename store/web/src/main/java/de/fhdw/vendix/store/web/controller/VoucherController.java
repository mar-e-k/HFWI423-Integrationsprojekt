package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.voucher.VoucherDTO;
import de.fhdw.vendix.commons.spring.web.api.store.VoucherApi;
import de.fhdw.vendix.store.core.domain.voucher.*;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
public class VoucherController implements VoucherApi {

    private final VoucherService voucherService;
    private final VoucherMapper  voucherMapper;

    public VoucherController(VoucherService voucherService, VoucherMapper voucherMapper) {
        this.voucherService = voucherService;
        this.voucherMapper  = voucherMapper;
    }

    @Override
    public ResponseEntity<VoucherDTO> getVoucherByCode(UUID code) {
        Optional<VoucherDTO> voucher = voucherService.findByCode(code).map(voucherMapper::toDTO);
        return ResponseEntity.of(voucher);
    }

    @Override
    public ResponseEntity<VoucherDTO> redeemVoucherByCode(UUID code) {
        return voucherService.findByCode(code)
                .map(this::redeemVoucher)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<VoucherDTO> redeemVoucher(Voucher voucher) {
        try {
            Voucher redeemed = voucher.redeem();
            Voucher saved = voucherService.update(redeemed);
            return ResponseEntity.ok(voucherMapper.toDTO(saved));
        } catch (VoucherExpiredException | VoucherAlreadyRedeemedException | OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}