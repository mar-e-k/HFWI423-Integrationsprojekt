package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.ReceiptResponseDTO;
import de.fhdw.vendix.commons.spring.web.api.store.ReceiptApi;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptDTOMapper;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptMapper;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptService;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLineMapper;
import de.fhdw.vendix.store.core.domain.voucher.Voucher;
import de.fhdw.vendix.store.core.domain.voucher.VoucherMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class ReceiptController implements ReceiptApi {

    private final ReceiptService receiptService;
    private final ReceiptMapper  receiptMapper;
    private final ReceiptDTOMapper receiptDTOMapper;
    private final ReceiptLineMapper receiptLineMapper;
    private final VoucherMapper voucherMapper;

    ReceiptController(
            ReceiptService receiptService,
            ReceiptMapper receiptMapper,
            ReceiptDTOMapper receiptDTOMapper,
            ReceiptLineMapper receiptLineMapper,
            VoucherMapper voucherMapper
    ) {
        this.receiptService = receiptService;
        this.receiptMapper  = receiptMapper;
        this.receiptDTOMapper = receiptDTOMapper;
        this.receiptLineMapper = receiptLineMapper;
        this.voucherMapper = voucherMapper;
    }

    @Override
    public ResponseEntity<List<ReceiptResponseDTO>> getReceipts() {
        List<ReceiptResponseDTO> receipts = receiptService.findAll().stream()
                .map(receiptMapper::toDTO)
                .map(receiptDTOMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(receipts);
    }

    @Override
    public ResponseEntity<ReceiptResponseDTO> postReceipt(ReceiptRequestDTO receiptRequestDTO) {
        ReceiptDTO receiptDTO = receiptDTOMapper.toDomainDTO(receiptRequestDTO);
        Receipt receipt = receiptMapper.toEntity(receiptDTO);
        Receipt created = receiptService.create(receipt);
        ReceiptDTO createdDTO = receiptMapper.toDTO(created);
        ReceiptResponseDTO response = receiptDTOMapper.toResponseDTO(createdDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ReceiptResponseDTO> checkoutReceipt(ReceiptRequestDTO receiptRequestDTO) {
        ReceiptDTO receiptDTO = receiptDTOMapper.toDomainDTO(receiptRequestDTO);
        Receipt receipt = receiptMapper.toEntity(receiptDTO);
        List<ReceiptLine> receiptLines = receiptLineMapper.toEntities(receiptRequestDTO.lines());
        List<Voucher> vouchers = voucherMapper.toEntities(receiptRequestDTO.vouchers());
        Receipt created = receiptService.checkoutReceipt(receipt, receiptLines, vouchers);
        ReceiptDTO createdDTO = receiptMapper.toDTO(created);
        ReceiptResponseDTO response = receiptDTOMapper.toResponseDTO(createdDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ReceiptResponseDTO> printReceipt(Long receiptId) {
        Receipt printed = receiptService.printReceipt(receiptId);
        ReceiptDTO printedDTO = receiptMapper.toDTO(printed);
        ReceiptResponseDTO response = receiptDTOMapper.toResponseDTO(printedDTO);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ReceiptResponseDTO> cancelReceipt(Long receiptId) {
        Receipt printed = receiptService.cancelReceipt(receiptId);
        ReceiptDTO printedDTO = receiptMapper.toDTO(printed);
        ReceiptResponseDTO response = receiptDTOMapper.toResponseDTO(printedDTO);
        return ResponseEntity.ok(response);
    }
}