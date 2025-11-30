package de.fhdw.fillialensystem.api.controller;

import de.fhdw.commons.api.dto.ReceiptDTO;
import de.fhdw.commons.api.dto.ReceiptLinkArticleDTO;
import de.fhdw.commons.utility.AuthContext;
import de.fhdw.fillialensystem.api.mapper.ReceiptLinkArticleMapper;
import de.fhdw.fillialensystem.api.mapper.ReceiptMapper;
import de.fhdw.fillialensystem.persistence.service.ReceiptService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receipt")
@Tag(name = "Receipt", description = "Endpoints for operations related to receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    private final ReceiptMapper receiptMapper;
    private final ReceiptLinkArticleMapper receiptLinkArticleMapper;

    public ReceiptController(ReceiptService receiptService, ReceiptMapper receiptMapper, ReceiptLinkArticleMapper receiptLinkArticleMapper) {
        this.receiptService = receiptService;
        this.receiptMapper = receiptMapper;
        this.receiptLinkArticleMapper = receiptLinkArticleMapper;
    }

    @GetMapping
    public ResponseEntity<List<ReceiptDTO>> findAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(receiptService.findAll()
                        .stream()
                        .map(receiptMapper::toDto)
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptDTO> findById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(receiptService.findById(id)
                        .map(receiptMapper::toDto)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @PostMapping
    public ResponseEntity<ReceiptDTO> createReceipt(@RequestBody List<ReceiptLinkArticleDTO> receiptArticles, @AuthenticationPrincipal AuthContext authContext) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(receiptMapper.toDto(
                        receiptService.createReceipt(authContext.getRegisterId(), authContext.getUuid(), receiptArticles
                                .stream()
                                .map(receiptLinkArticleMapper::toEntity)
                                .toList())));
    }
}