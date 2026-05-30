package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.voucher.VoucherDTO;
import de.fhdw.vendix.commons.api.domain.voucher.VoucherRequestDTO;
import de.fhdw.vendix.commons.api.domain.voucher.VoucherResponseDTO;
import de.fhdw.vendix.commons.spring.web.api.store.VoucherApi;
import de.fhdw.vendix.store.core.domain.voucher.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
class VoucherController implements VoucherApi {

    private final VoucherService voucherService;
    private final VoucherMapper voucherMapper;
    private final VoucherDTOMapper voucherDTOMapper;

    public VoucherController(VoucherService voucherService, VoucherMapper voucherMapper, VoucherDTOMapper voucherDTOMapper) {
        this.voucherService = voucherService;
        this.voucherMapper = voucherMapper;
        this.voucherDTOMapper = voucherDTOMapper;
    }

    @Override
    public ResponseEntity<VoucherResponseDTO> getVoucherByCode(UUID voucherCode) {
        Optional<VoucherResponseDTO> voucher = voucherService.findByVoucherCode(voucherCode)
                .map(voucherMapper::toDTO)
                .map(voucherDTOMapper::toResponseDTO);
        return ResponseEntity.of(voucher);
    }


    @Override
    public ResponseEntity<VoucherResponseDTO> createVoucher(VoucherRequestDTO requestDTO) {
        VoucherDTO voucherDTO = voucherDTOMapper.toDomainDTO(requestDTO);
        Voucher voucher = voucherMapper.toEntity(voucherDTO);
        Voucher created = voucherService.create(voucher);
        VoucherDTO createdDTO = voucherMapper.toDTO(created);
        VoucherResponseDTO response = voucherDTOMapper.toResponseDTO(createdDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<VoucherResponseDTO> redeemVoucherByCode(UUID voucherCode) {
        Optional<Voucher> voucher = voucherService.findByVoucherCode(voucherCode);

        if (voucher.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Voucher redeemedVoucher = voucher.get().redeem();
        Voucher updatedVoucher = voucherService.update(redeemedVoucher);
        VoucherDTO updatedVoucherDTO = voucherMapper.toDTO(updatedVoucher);
        VoucherResponseDTO responseDTO = voucherDTOMapper.toResponseDTO(updatedVoucherDTO);

        return ResponseEntity.ok(responseDTO);
    }
}