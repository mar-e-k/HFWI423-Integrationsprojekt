package com.example.application.api.load;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.goodsreceipts.GoodsReceipt;
import com.example.application.data.goodsreceipts.GoodsReceiptItem;
import com.example.application.data.goodsreceipts.GoodsReceiptItemStatus;
import com.example.application.data.goodsreceipts.GoodsReceiptStatus;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.services.ArticleInfoService;
import com.example.application.services.GoodsReceiptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    record CreateFromOrdersRequest(List<Long> restockOrderIds, String supplierName, String deliveryNoteNumber, LocalDate deliveryDate) {}
    record AddItemRequest(Long articleId, Integer expectedQty, Integer actualQty, String defectNotes) {}
    record UpdateItemRequest(Integer actualQty, String defectNotes) {}
    record SetItemStatusRequest(String status) {}
    record ItemSummary(Long itemId, Integer qty) {}

    /** GET /api/load/goods-receipts – alle Wareneingaenge */
    @GetMapping
    public List<GoodsReceipt> goodsReceipts() {
        return goodsReceiptService.findAll();
    }

    /** GET /api/load/goods-receipts/{id} – einzelnen Wareneingang laden (Detailansicht) */
    @GetMapping("/{id}")
    public GoodsReceipt getById(@PathVariable Long id) {
        try {
            return goodsReceiptService.getById(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /** GET /api/load/goods-receipts/pending-ids – IDs aller Wareneingaenge im Status IN_PRUEFUNG */
    @GetMapping("/pending-ids")
    public List<Long> pendingIds() {
        return goodsReceiptService.findAll().stream()
                .filter(gr -> gr.getStatus() == GoodsReceiptStatus.IN_PRUEFUNG)
                .map(GoodsReceipt::getId)
                .toList();
    }

    /** GET /api/load/goods-receipts/{id}/item-summaries – itemId + qty jeder Position (fuer JMeter RegexExtractor) */
    @GetMapping("/{id}/item-summaries")
    public List<ItemSummary> itemSummaries(@PathVariable Long id) {
        return goodsReceiptService.getItemsForReceipt(id).stream()
                .map(item -> new ItemSummary(item.getId(), item.getActualQuantity()))
                .toList();
    }

    /** GET /api/load/goods-receipts/open-orders – offene Bestellungen fuer Wareneingang-Dialog */
    @GetMapping("/open-orders")
    public List<RestockOrder> openOrders() {
        return goodsReceiptService.findOpenRestockOrders();
    }

    /**
     * POST /api/load/goods-receipts/from-orders – Wareneingang aus ausgewaehlten Bestellungen anlegen.
     * Simuliert: User waehlt einen Artikel im Dialog aus und klickt Anlegen.
     */
    @PostMapping("/from-orders")
    @ResponseStatus(HttpStatus.CREATED)
    public GoodsReceipt createFromOrders(@RequestBody CreateFromOrdersRequest req) {
        if (req.restockOrderIds() == null || req.restockOrderIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Keine Bestellungen ausgewaehlt");
        }
        LocalDate date = req.deliveryDate() != null ? req.deliveryDate() : LocalDate.now();
        String supplier = req.supplierName() != null ? req.supplierName() : "Lasttest-Lieferant";
        String note = req.deliveryNoteNumber() != null ? req.deliveryNoteNumber() : "LT-" + System.currentTimeMillis();
        try {
            return goodsReceiptService.createFromRestockOrders(req.restockOrderIds(), supplier, note, date);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /**
     * POST /api/load/goods-receipts/from-next-batch
     * Legt einen Wareneingang aus den naechsten 10 offenen Bestellungen an.
     * Simuliert: User waehlt 10 Artikel im Dialog aus und klickt Anlegen.
     * 201 + GoodsReceipt -> Wareneingang angelegt
     * 204 No Content    -> keine offene Bestellung vorhanden
     */
    @PostMapping("/from-next-batch")
    public ResponseEntity<GoodsReceipt> createFromNextBatch(@RequestBody CreateReceiptRequest req) {
        LocalDate date = req.deliveryDate() != null ? req.deliveryDate() : LocalDate.now();
        String supplier = req.supplierName() != null ? req.supplierName() : "Lasttest-Lieferant";
        String note = req.deliveryNoteNumber() != null ? req.deliveryNoteNumber() : "LT-" + System.currentTimeMillis();
        GoodsReceipt receipt = goodsReceiptService.createFromNextBatch(10, supplier, note, date);
        if (receipt == null) {
            System.out.println("[from-next-batch] 204 - keine Bestellung verfuegbar");
            return ResponseEntity.noContent().build();
        }
        System.out.println("[from-next-batch] 201 receiptId=" + receipt.getId() + " items=" + receipt.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }

    /**
     * POST /api/load/goods-receipts/from-next-order
     * Legt einen Wareneingang aus der naechsten offenen Bestellung an.
     * Simuliert: User oeffnet Dialog, klickt ersten Artikel an, klickt Anlegen.
     * 201 + GoodsReceipt  -> Wareneingang angelegt
     * 204 No Content      -> keine offene Bestellung vorhanden
     */
    @PostMapping("/from-next-order")
    public ResponseEntity<GoodsReceipt> createFromNextOrder(@RequestBody CreateReceiptRequest req) {
        List<RestockOrder> open = goodsReceiptService.findOpenRestockOrders();
        System.out.println("[from-next-order] offeneBestellungen=" + open.size());
        if (open.isEmpty()) {
            System.out.println("[from-next-order] 204 – keine offene Bestellung");
            return ResponseEntity.noContent().build();
        }
        LocalDate date = req.deliveryDate() != null ? req.deliveryDate() : LocalDate.now();
        String supplier = req.supplierName() != null ? req.supplierName() : "Lasttest-Lieferant";
        String note = req.deliveryNoteNumber() != null ? req.deliveryNoteNumber() : "LT-" + System.currentTimeMillis();
        for (RestockOrder order : open) {
            try {
                GoodsReceipt receipt = goodsReceiptService.createFromRestockOrders(
                        List.of(order.getId()), supplier, note, date);
                System.out.println("[from-next-order] 201 orderId=" + order.getId() + " article=" + order.getArticleNumber());
                return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
            } catch (Exception e) {
                System.out.println("[from-next-order] SKIP orderId=" + order.getId() + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        System.out.println("[from-next-order] 204 – alle Bestellungen fehlgeschlagen");
        return ResponseEntity.noContent().build();
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

    /**
     * POST /api/load/goods-receipts/{id}/approve-all-items
     * Setzt alle Items eines Wareneingangs auf FREIGEGEBEN.
     * Ersetzt den fehleranfaelligen JMeter-ForeachController fuer den Lasttest.
     * 200 + Anzahl freigegebener Items
     */
    @PostMapping("/{id}/approve-all-items")
    public ResponseEntity<Integer> approveAllItems(@PathVariable Long id) {
        List<GoodsReceiptItem> items = goodsReceiptService.getItemsForReceipt(id);
        int approved = 0;
        for (GoodsReceiptItem item : items) {
            if (item.getStatus() == GoodsReceiptItemStatus.IN_PRUEFUNG) {
                goodsReceiptService.setItemStatus(item.getId(), GoodsReceiptItemStatus.FREIGEGEBEN);
                approved++;
            }
        }
        System.out.println("[approve-all-items] receiptId=" + id + " approved=" + approved);
        return ResponseEntity.ok(approved);
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
