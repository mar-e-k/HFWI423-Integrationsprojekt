package com.example.application.api.load;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.goodsreceipts.GoodsReceipt;
import com.example.application.data.goodsreceipts.GoodsReceiptItem;
import com.example.application.data.goodsreceipts.GoodsReceiptItemStatus;
import com.example.application.services.ArticleInfoService;
import com.example.application.services.GoodsReceiptService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

/**
 * Lasttest-Endpunkte für GoodsReceiptView.
 * Basis-URL: /api/load/goods-receipts
 */
@RestController
@RequestMapping("/api/load/goods-receipts")
public class LoadTestGoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;
    private final ArticleInfoService articleInfoService;

    public LoadTestGoodsReceiptController(GoodsReceiptService goodsReceiptService,
                                           ArticleInfoService articleInfoService) {
        this.goodsReceiptService = goodsReceiptService;
        this.articleInfoService = articleInfoService;
    }

    record CreateReceiptRequest(String supplierName, String deliveryNoteNumber, LocalDate deliveryDate) {}
    record AddItemRequest(Long articleId, Integer expectedQty, Integer actualQty, String defectNotes) {}
    record UpdateItemRequest(Integer actualQty, String defectNotes) {}
    record SetItemStatusRequest(String status) {}

    /** GET /api/load/goods-receipts – alle Wareneingaenge */
    @GetMapping
    public List<GoodsReceipt> goodsReceipts() {
        return goodsReceiptService.findAll();
    }

    /** POST /api/load/goods-receipts – neuen Wareneingang anlegen */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoodsReceipt createGoodsReceipt(@RequestBody CreateReceiptRequest req) {
        LocalDate date = req.deliveryDate() != null ? req.deliveryDate() : LocalDate.now();
        String supplier = req.supplierName() != null ? req.supplierName() : "Lasttest-Lieferant";
        String note = req.deliveryNoteNumber() != null ? req.deliveryNoteNumber() : "LT-" + System.currentTimeMillis();
        return goodsReceiptService.create(supplier, note, date);
    }

    /** POST /api/load/goods-receipts/{id}/items – Position hinzufügen */
    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public GoodsReceiptItem addItem(
            @PathVariable Long id,
            @RequestBody AddItemRequest req) {
        try {
            ArticleInfo article = articleInfoService.findById(req.articleId());
            return goodsReceiptService.addItemToReceipt(id, article, req.expectedQty(), req.actualQty(), req.defectNotes());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** PUT /api/load/goods-receipts/{id}/items/{itemId} – Position aktualisieren */
    @PutMapping("/{id}/items/{itemId}")
    public GoodsReceiptItem updateItem(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestBody UpdateItemRequest req) {
        try {
            return goodsReceiptService.updateItem(itemId, req.actualQty(), req.defectNotes());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** PUT /api/load/goods-receipts/{id}/items/{itemId}/status – Position freigeben oder sperren */
    @PutMapping("/{id}/items/{itemId}/status")
    public GoodsReceiptItem setItemStatus(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestBody SetItemStatusRequest req) {
        GoodsReceiptItemStatus status;
        try {
            status = GoodsReceiptItemStatus.valueOf(req.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unbekannter Status: " + req.status() + ". Erlaubt: " +
                    java.util.Arrays.toString(GoodsReceiptItemStatus.values()));
        }
        try {
            return goodsReceiptService.setItemStatus(itemId, status);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** POST /api/load/goods-receipts/{id}/complete – Prüfung abschließen */
    @PostMapping("/{id}/complete")
    public GoodsReceipt completeInspection(@PathVariable Long id) {
        try {
            return goodsReceiptService.completeInspection(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /** DELETE /api/load/goods-receipts/{id} – Wareneingang löschen */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGoodsReceipt(@PathVariable Long id) {
        try {
            goodsReceiptService.deleteIfAllowed(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
