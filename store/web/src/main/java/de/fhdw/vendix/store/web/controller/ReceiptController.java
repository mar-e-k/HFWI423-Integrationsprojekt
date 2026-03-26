package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.receipt.web.ReceiptEndpoints;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptDTO;
import de.fhdw.vendix.commons.api.domain.receipt.dto.ReceiptRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.port.ReceiptCommandPort;
import de.fhdw.vendix.commons.spring.core.mapper.dto.ReceiptDTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ReceiptEndpoints.BASE)
class ReceiptController {

    private final ReceiptDTOMapper receiptDTOMapper;
    private final ReceiptCommandPort receiptCommandPort;

    public ReceiptController(ReceiptDTOMapper receiptDTOMapper, ReceiptCommandPort receiptCommandPort) {
        this.receiptDTOMapper = receiptDTOMapper;
        this.receiptCommandPort = receiptCommandPort;
    }

    @PostMapping
    public ResponseEntity<ReceiptDTO> createReceipt(@RequestBody ReceiptRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(receiptCommandPort.create(receiptDTOMapper.toDomainDTO(dto)));
    }
}