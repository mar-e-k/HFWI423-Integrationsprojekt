package de.fhdw.fillialensystem.api.controller;

import de.fhdw.commons.api.dto.ReceiptDTO;
import de.fhdw.commons.api.dto.ReceiptLinkArticleDTO;
import de.fhdw.commons.utility.AuthContext;
import de.fhdw.fillialensystem.api.mapper.ReceiptLinkArticleMapper;
import de.fhdw.fillialensystem.api.mapper.ReceiptMapper;
import de.fhdw.fillialensystem.persistence.entity.Store;
import de.fhdw.fillialensystem.persistence.entity.StoreLinkStock;
import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import de.fhdw.fillialensystem.persistence.service.ReceiptService;
import de.fhdw.fillialensystem.persistence.service.StoreLinkStockService;
import de.fhdw.fillialensystem.persistence.service.StoreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/receipt")
@Tag(name = "Receipt", description = "Endpoints for operations related to receipts")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final StoreLinkStockService storeLinkStockService;
    private final StoreService storeService;
    private final ReceiptMapper receiptMapper;
    private final ReceiptLinkArticleMapper receiptLinkArticleMapper;

    public ReceiptController(ReceiptService receiptService, StoreLinkStockService storeLinkStockService, StoreService storeService, ReceiptMapper receiptMapper, ReceiptLinkArticleMapper receiptLinkArticleMapper) {
        this.receiptService = receiptService;
        this.storeLinkStockService = storeLinkStockService;
        this.storeService = storeService;
        this.receiptMapper = receiptMapper;
        this.receiptLinkArticleMapper = receiptLinkArticleMapper;
    }

    @PostMapping
    public ResponseEntity<ReceiptDTO> createReceipt(@RequestBody List<ReceiptLinkArticleDTO> receiptArticles, @AuthenticationPrincipal AuthContext authContext) {
        // Lagerbestand dekrementieren
        Long storeId = authContext.getStoreId().longValue();
        if (storeId != null) {
            Optional<Store> storeOptional = storeService.findById(storeId);
            storeOptional.ifPresent(store -> {
                for (ReceiptLinkArticleDTO receiptArticleDTO : receiptArticles) {
                    Article article = new Article(receiptArticleDTO.getArticleId());
                    Optional<StoreLinkStock> storeLinkStockOptional = storeLinkStockService.findByStoreAndArticle(store, article);
                    storeLinkStockOptional.ifPresent(storeLinkStock -> {
                        int newAmount = storeLinkStock.getAmount() - receiptArticleDTO.getAmount();
                        storeLinkStock.setAmount(newAmount);
                        storeLinkStockService.update(storeLinkStock.getId(), storeLinkStock);
                    });
                }
            });
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(receiptMapper.toDto(
                        receiptService.createReceipt(authContext.getRegisterId().longValue(), authContext.getUuid(), receiptArticles
                                .stream()
                                .map(receiptLinkArticleMapper::toEntity)
                                .toList())));
    }

    @PostMapping("/redeem/{depositRedemptionCode}")
    public ResponseEntity<ReceiptDTO> redeemDepositReceipt(@PathVariable String depositRedemptionCode) {
        return ResponseEntity.ok(receiptMapper.toDto(receiptService.redeemDepositReceipt(depositRedemptionCode)));
    }
}
