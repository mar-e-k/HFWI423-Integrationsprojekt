package de.fhdw.vendix.store.api.controller;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptEndpoints;
import de.fhdw.vendix.commons.core.api.dto.ReceiptDTO;
import de.fhdw.vendix.commons.core.api.dto.ReceiptLinkArticleDTO;
import de.fhdw.vendix.commons.security.auth.AuthContext;
import de.fhdw.vendix.store.api.mapper.ReceiptLinkArticleMapper;
import de.fhdw.vendix.store.api.mapper.ReceiptMapper;
import de.fhdw.vendix.store.persistence.service.ReceiptService;
import de.fhdw.vendix.store.persistence.service.StoreLinkStockService;
import de.fhdw.vendix.store.persistence.service.StoreService;
import de.fhdw.vendix.store.persistence.service.imported.ArticleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ReceiptEndpoints.BASE)
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

    // TODO: das sollten wir von Beleg trennen. Seperate Entität -> Voucher ?

    @PostMapping("/redeem/{depositRedemptionCode}")
    public ResponseEntity<ReceiptDTO> redeemDepositReceipt(@PathVariable String depositRedemptionCode) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(receiptMapper
                        .toDto(receiptService.redeemDepositReceipt(depositRedemptionCode)));
    }
}
