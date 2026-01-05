package de.fhdw.fillialensystem.api.controller;

import de.fhdw.commons.api.dto.ReceiptDTO;
import de.fhdw.commons.api.dto.ReceiptLinkArticleDTO;
import de.fhdw.commons.security.utility.security.auth.AuthContext;
import de.fhdw.fillialensystem.api.mapper.ReceiptLinkArticleMapper;
import de.fhdw.fillialensystem.api.mapper.ReceiptMapper;
import de.fhdw.fillialensystem.persistence.service.ReceiptService;
import de.fhdw.fillialensystem.persistence.service.StoreLinkStockService;
import de.fhdw.fillialensystem.persistence.service.StoreService;
import de.fhdw.fillialensystem.persistence.service.imported.ArticleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receipt")
@Tag(name = "Receipt", description = "Endpoints for operations related to receipts")
public class ReceiptController {

    private final ArticleService articleService;
    private final ReceiptService receiptService;
    private final StoreLinkStockService storeLinkStockService;
    private final StoreService storeService;
    private final ReceiptMapper receiptMapper;
    private final ReceiptLinkArticleMapper receiptLinkArticleMapper;

    public ReceiptController(ArticleService articleService, ReceiptService receiptService, StoreLinkStockService storeLinkStockService, StoreService storeService, ReceiptMapper receiptMapper, ReceiptLinkArticleMapper receiptLinkArticleMapper) {
        this.articleService = articleService;
        this.receiptService = receiptService;
        this.storeLinkStockService = storeLinkStockService;
        this.storeService = storeService;
        this.receiptMapper = receiptMapper;
        this.receiptLinkArticleMapper = receiptLinkArticleMapper;
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

    @PostMapping("/redeem/{depositRedemptionCode}")
    public ResponseEntity<ReceiptDTO> redeemDepositReceipt(@PathVariable String depositRedemptionCode) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(receiptMapper
                        .toDto(receiptService.redeemDepositReceipt(depositRedemptionCode)));
    }
}
