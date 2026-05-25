package com.example.application.api.wareneingang;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.goodsreceipts.GoodsReceipt;
import com.example.application.data.goodsreceipts.GoodsReceiptItem;
import com.example.application.data.goodsreceipts.GoodsReceiptItemStatus;
import com.example.application.data.goodsreceipts.GoodsReceiptStatus;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.services.ArticleInfoService;
import com.example.application.services.GoodsReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/wareneingaenge")
@Tag(name = "Wareneingaenge", description = "Wareneingaenge pruefen und verwalten")
public class WareneingangController {

    private final GoodsReceiptService goodsReceiptService;
    private final ArticleInfoService articleInfoService;

    public WareneingangController(GoodsReceiptService goodsReceiptService,
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

    @Operation(summary = "Alle Wareneingaenge abrufen")
    @GetMapping
    public List<GoodsReceipt> goodsReceipts() {
        return goodsReceiptService.findAll();
    }

    @Operation(summary = "Einzelnen Wareneingang laden")
    @GetMapping("/{id}")
    public GoodsReceipt getById(@PathVariable Long id) {
        try {
            return goodsReceiptService.getById(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "IDs aller Wareneingaenge im Status IN_PRUEFUNG")
    @GetMapping("/pending-ids")
    public List<Long> pendingIds() {
        return goodsReceiptService.findAll().stream()
                .filter(gr -> gr.getStatus() == GoodsReceiptStatus.IN_PRUEFUNG)
                .map(GoodsReceipt::getId)
                .toList();
    }

    @Operation(summary = "Item-Zusammenfassung eines Wareneingangs")
    @GetMapping("/{id}/item-summaries")
    public List<ItemSummary> itemSummaries(@PathVariable Long id) {
        return goodsReceiptService.getItemsForReceipt(id).stream()
                .map(item -> new ItemSummary(item.getId(), item.getActualQuantity()))
                .toList();
    }

    @Operation(summary = "Offene Bestellungen fuer Wareneingang-Dialog")
    @GetMapping("/open-orders")
    public List<RestockOrder> openOrders() {
        return goodsReceiptService.findOpenRestockOrders();
    }

    @Operation(summary = "Wareneingang aus ausgewaehlten Bestellungen anlegen")
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

    @Operation(summary = "Wareneingang aus naechsten 10 offenen Bestellungen anlegen")
    @PostMapping("/from-next-batch")
    public ResponseEntity<GoodsReceipt> createFromNextBatch(@RequestBody CreateReceiptRequest req) {
        LocalDate date = req.deliveryDate() != null ? req.deliveryDate() : LocalDate.now();
        String supplier = req.supplierName() != null ? req.supplierName() : "Lasttest-Lieferant";
        String note = req.deliveryNoteNumber() != null ? req.deliveryNoteNumber() : "LT-" + System.currentTimeMillis();
        GoodsReceipt receipt = goodsReceiptService.createFromNextBatch(10, supplier, note, date);
        if (receipt == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }

    @Operation(summary = "Wareneingang aus naechster einzelner offener Bestellung anlegen")
    @PostMapping("/from-next-order")
    public ResponseEntity<GoodsReceipt> createFromNextOrder(@RequestBody CreateReceiptRequest req) {
        List<RestockOrder> open = goodsReceiptService.findOpenRestockOrders();
        if (open.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        LocalDate date = req.deliveryDate() != null ? req.deliveryDate() : LocalDate.now();
        String supplier = req.supplierName() != null ? req.supplierName() : "Lasttest-Lieferant";
        String note = req.deliveryNoteNumber() != null ? req.deliveryNoteNumber() : "LT-" + System.currentTimeMillis();
        for (RestockOrder order : open) {
            try {
                GoodsReceipt receipt = goodsReceiptService.createFromRestockOrders(
                        List.of(order.getId()), supplier, note, date);
                return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
            } catch (Exception e) {
                System.out.println("[from-next-order] SKIP orderId=" + order.getId() + ": " + e.getMessage());
            }
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Neuen Wareneingang anlegen")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoodsReceipt createGoodsReceipt(@RequestBody CreateReceiptRequest req) {
        LocalDate date = req.deliveryDate() != null ? req.deliveryDate() : LocalDate.now();
        String supplier = req.supplierName() != null ? req.supplierName() : "Lasttest-Lieferant";
        String note = req.deliveryNoteNumber() != null ? req.deliveryNoteNumber() : "LT-" + System.currentTimeMillis();
        return goodsReceiptService.create(supplier, note, date);
    }

    @Operation(summary = "Position zu Wareneingang hinzufuegen")
    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public GoodsReceiptItem addItem(@PathVariable Long id, @RequestBody AddItemRequest req) {
        try {
            ArticleInfo article = articleInfoService.findById(req.articleId());
            return goodsReceiptService.addItemToReceipt(id, article, req.expectedQty(), req.actualQty(), req.defectNotes());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Position aktualisieren")
    @PutMapping("/{id}/items/{itemId}")
    public GoodsReceiptItem updateItem(@PathVariable Long id, @PathVariable Long itemId,
                                        @RequestBody UpdateItemRequest req) {
        try {
            return goodsReceiptService.updateItem(itemId, req.actualQty(), req.defectNotes());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Status einer Position setzen (FREIGEGEBEN / GESPERRT)")
    @PutMapping("/{id}/items/{itemId}/status")
    public GoodsReceiptItem setItemStatus(@PathVariable Long id, @PathVariable Long itemId,
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

    @Operation(summary = "Alle Positionen eines Wareneingangs freigeben")
    @PostMapping("/{id}/approve-all-items")
    public ResponseEntity<Integer> approveAllItems(@PathVariable Long id) {
        try {
            int approved = goodsReceiptService.approveAllItemsForReceipt(id);
            return ResponseEntity.ok(approved);
        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.ok(0);
        }
    }

    @Operation(summary = "Pruefung abschliessen")
    @PostMapping("/{id}/complete")
    public GoodsReceipt completeInspection(@PathVariable Long id) {
        try {
            return goodsReceiptService.completeInspection(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (ObjectOptimisticLockingFailureException e) {
            return goodsReceiptService.getById(id);
        }
    }

    @Operation(summary = "Wareneingang loeschen")
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
