package com.example.application.services;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.goodsreceipts.GoodsReceipt;
import com.example.application.data.goodsreceipts.GoodsReceiptItem;
import com.example.application.data.goodsreceipts.GoodsReceiptItemRepository;
import com.example.application.data.goodsreceipts.GoodsReceiptItemStatus;
import com.example.application.data.goodsreceipts.GoodsReceiptRepository;
import com.example.application.data.goodsreceipts.GoodsReceiptStatus;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.data.restockorder.RestockOrderRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoodsReceiptService {

    private final GoodsReceiptRepository receiptRepo;
    private final GoodsReceiptItemRepository itemRepo;
    private final ArticleInfoRepository articleRepo;
    private final RestockOrderRepository restockOrderRepo;
    private final JdbcTemplate jdbc;

    public GoodsReceiptService(GoodsReceiptRepository receiptRepo,
                               GoodsReceiptItemRepository itemRepo,
                               ArticleInfoRepository articleRepo,
                               RestockOrderRepository restockOrderRepo,
                               JdbcTemplate jdbc) {
        this.receiptRepo = receiptRepo;
        this.itemRepo = itemRepo;
        this.articleRepo = articleRepo;
        this.restockOrderRepo = restockOrderRepo;
        this.jdbc = jdbc;
    }

    // ------------------------------------------------------------------------
    // Nummernkreis
    // ------------------------------------------------------------------------

    // Zieht die nächste Zahl aus goods_receipt_seq und formatiert WE-YYYY-00001
    private String nextReceiptNumber() {
        Long next = jdbc.queryForObject("select nextval('goods_receipt_seq')", Long.class);
        String year = String.valueOf(Year.now().getValue());
        return String.format("WE-%s-%05d", year, next);
    }

    // ------------------------------------------------------------------------
    // CRUD Wareneingang (manuell angelegt)
    // ------------------------------------------------------------------------

    @Transactional
    public GoodsReceipt create(String supplierName,
                               String deliveryNoteNumber,
                               LocalDate deliveryDate) {
        GoodsReceipt gr = new GoodsReceipt();
        gr.setReceiptNumber(nextReceiptNumber());
        gr.setSupplierName(supplierName);
        gr.setDeliveryNoteNumber(deliveryNoteNumber);
        gr.setDeliveryDate(deliveryDate);
        gr.setStatus(GoodsReceiptStatus.IN_PRUEFUNG);
        return receiptRepo.save(gr);
    }

    @Transactional(readOnly = true)
    public List<GoodsReceipt> findAll() {
        return receiptRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<String> findDistinctSupplierNames() {
        return receiptRepo.findDistinctSupplierNames();
    }

    @Transactional(readOnly = true)
    public GoodsReceipt getById(Long id) {
        return receiptRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Wareneingang " + id + " nicht gefunden"));
    }

    @Transactional
    public GoodsReceipt updateStatus(Long id, GoodsReceiptStatus status) {
        GoodsReceipt gr = getById(id);
        gr.setStatus(status);
        return receiptRepo.save(gr);
    }

    /** Loescht alle Wareneingaenge inkl. aller Positionen. */
    @Transactional
    public int deleteAll() {
        List<GoodsReceipt> all = receiptRepo.findAll();
        for (GoodsReceipt gr : all) {
            itemRepo.deleteByGoodsReceiptId(gr.getId());
        }
        receiptRepo.deleteAll(all);
        return all.size();
    }

    /**
     * Löscht einen Wareneingang inkl. aller Items,
     * aber nur wenn er noch im Status IN_PRUEFUNG ist.
     */
    @Transactional
    public void deleteIfAllowed(Long id) {
        GoodsReceipt gr = getById(id);
        if (gr.getStatus() == GoodsReceiptStatus.IN_PRUEFUNG) {
            itemRepo.deleteByGoodsReceiptId(id);
            receiptRepo.delete(gr);
        } else {
            throw new IllegalStateException(
                    "Wareneingang kann nicht gelöscht werden, Status ist " + gr.getStatus()
                            + " (nur IN_PRUEFUNG darf gelöscht werden)");
        }
    }

    // ------------------------------------------------------------------------
    // Restock-Orders (Schnittstelle aus der Logistik)
    // ------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<RestockOrder> findOpenRestockOrders() {
        // alle genehmigten, aber noch nicht gelieferten Bestellungen
        return restockOrderRepo.findByDeliveredFalseAndApprovedTrue();
    }

    /**
     * Legt einen Wareneingang aus den naechsten (bis zu maxCount) offenen Bestellungen an.
     * Simuliert: User waehlt mehrere Bestellungen im Dialog aus und klickt Anlegen.
     * Jede Bestellung wird atomar per markDeliveredIfOpen beansprucht – Race-Condition-sicher.
     * Gibt null zurueck wenn keine einzige Bestellung mehr verfuegbar war.
     */
    @Transactional
    public GoodsReceipt createFromNextBatch(int maxCount, String supplierName,
                                             String deliveryNoteNumber, LocalDate deliveryDate) {
        List<RestockOrder> open = restockOrderRepo.findByDeliveredFalseAndApprovedTrue();
        System.out.println("[createFromNextBatch] offeneBestellungen=" + open.size());

        // Phase 1: bis zu maxCount Bestellungen atomar beanspruchen
        List<Long>        claimedIds      = new java.util.ArrayList<>();
        List<ArticleInfo> claimedArticles = new java.util.ArrayList<>();
        List<Integer>     claimedPallets  = new java.util.ArrayList<>();

        for (RestockOrder ro : open) {
            if (claimedIds.size() >= maxCount) break;

            int updated = restockOrderRepo.markDeliveredIfOpen(ro.getId());
            if (updated == 0) continue; // anderer Thread war schneller

            ArticleInfo article = articleRepo.findByArticleNumber(ro.getArticleNumber());
            if (article == null) {
                System.out.println("[createFromNextBatch] SKIP kein Artikel: " + ro.getArticleNumber());
                continue;
            }
            Integer ppp = article.getPiecesPerPallet();
            if (ppp == null || ppp <= 0) {
                System.out.println("[createFromNextBatch] SKIP piecesPerPallet ungueltig: " + ro.getArticleNumber());
                continue;
            }
            int pallets = ro.getQuantity() != null ? ro.getQuantity() / ppp : 0;
            claimedIds.add(ro.getId());
            claimedArticles.add(article);
            claimedPallets.add(pallets);
        }

        if (claimedIds.isEmpty()) {
            System.out.println("[createFromNextBatch] keine Bestellungen verfuegbar -> null");
            return null;
        }

        // Phase 2: Wareneingang anlegen
        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setReceiptNumber(nextReceiptNumber());
        receipt.setSupplierName(supplierName);
        receipt.setDeliveryNoteNumber(deliveryNoteNumber);
        receipt.setDeliveryDate(deliveryDate);
        receipt.setStatus(GoodsReceiptStatus.IN_PRUEFUNG);
        receipt = receiptRepo.save(receipt);

        for (int i = 0; i < claimedIds.size(); i++) {
            GoodsReceiptItem item = new GoodsReceiptItem();
            item.setGoodsReceipt(receipt);
            item.setArticle(claimedArticles.get(i));
            item.setExpectedQuantity(claimedPallets.get(i));
            item.setActualQuantity(claimedPallets.get(i));
            item.setDefectNotes(null);
            item.setStatus(GoodsReceiptItemStatus.IN_PRUEFUNG);
            itemRepo.save(item);
        }

        recomputeReceiptStatus(receipt);
        System.out.println("[createFromNextBatch] receiptId=" + receipt.getId() + " items=" + claimedIds.size());
        return receipt;
    }

    /**
     * Legt einen neuen Wareneingang aus einer Liste von RestockOrders an.
     * Für jede RestockOrder wird eine Prüfposition (GoodsReceiptItem) erzeugt.
     *
     * WICHTIG:
     * - RestockOrder.quantity = Stück
     * - Ab hier werden GoodsReceiptItem-Mengen in PALLETTEN geführt
     *   pallets = quantity / piecesPerPallet
     *   Reststücke werden ignoriert.
     */
    @Transactional
    public GoodsReceipt createFromRestockOrders(List<Long> restockOrderIds,
                                                String supplierName,
                                                String deliveryNoteNumber,
                                                LocalDate deliveryDate) {

        // 1) Wareneingang anlegen
        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setReceiptNumber(nextReceiptNumber());
        receipt.setSupplierName(supplierName);
        receipt.setDeliveryNoteNumber(deliveryNoteNumber);
        receipt.setDeliveryDate(deliveryDate);
        receipt.setStatus(GoodsReceiptStatus.IN_PRUEFUNG);
        receipt = receiptRepo.save(receipt);

        // 2) Für jede RestockOrder ein Item erzeugen (in PALLETTEN)
        for (Long roId : restockOrderIds) {
            // Atomisches UPDATE: setzt delivered=true nur wenn es noch false ist.
            // Gibt 0 zurueck wenn ein anderer Thread diese Order bereits verarbeitet hat.
            int updated = restockOrderRepo.markDeliveredIfOpen(roId);
            if (updated == 0) {
                throw new IllegalStateException("RestockOrder " + roId + " wurde bereits geliefert");
            }
            RestockOrder ro = restockOrderRepo.findById(roId)
                    .orElseThrow(() -> new IllegalArgumentException("RestockOrder " + roId + " nicht gefunden"));

            ArticleInfo article = articleRepo.findByArticleNumber(ro.getArticleNumber());
            if (article == null) {
                throw new IllegalArgumentException(
                        "ArticleInfo mit article_number=" + ro.getArticleNumber() + " nicht gefunden");
            }

            int pieces = ro.getQuantity() != null ? ro.getQuantity() : 0;

            Integer pppObj = article.getPiecesPerPallet();
            if (pppObj == null || pppObj <= 0) {
                throw new IllegalStateException(
                        "Artikel " + article.getArticleNumber() + " hat keinen gültigen Wert für pieces_per_pallet.");
            }
            int piecesPerPallet = pppObj;

            // Paletten berechnen, Reststücke ignorieren
            int pallets = pieces / piecesPerPallet;

            GoodsReceiptItem item = new GoodsReceiptItem();
            item.setGoodsReceipt(receipt);
            item.setArticle(article);
            item.setExpectedQuantity(pallets);   // PALLETTEN
            item.setActualQuantity(pallets);     // Startwert = Soll-Menge in Paletten
            item.setDefectNotes(null);
            item.setStatus(GoodsReceiptItemStatus.IN_PRUEFUNG);

            itemRepo.save(item);
            // delivered=true wurde bereits atomar per markDeliveredIfOpen gesetzt
        }

        // 3) Status des Wareneingangs initial ableiten
        recomputeReceiptStatus(receipt);

        return receipt;
    }


    // ------------------------------------------------------------------------
    // Items / Prüfpositionen
    // ------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<GoodsReceiptItem> getItemsForReceipt(Long receiptId) {
        return itemRepo.findByGoodsReceiptId(receiptId);
    }

    @Transactional
    public GoodsReceiptItem addItemToReceipt(Long receiptId,
                                             ArticleInfo article,
                                             Integer expectedQty,
                                             Integer actualQty,
                                             String defectNotes) {
        GoodsReceipt receipt = getById(receiptId);

        GoodsReceiptItem item = new GoodsReceiptItem();
        item.setGoodsReceipt(receipt);
        item.setArticle(article);
        item.setExpectedQuantity(expectedQty);
        item.setActualQuantity(actualQty != null ? actualQty : expectedQty);
        item.setDefectNotes(defectNotes);
        item.setStatus(GoodsReceiptItemStatus.IN_PRUEFUNG);

        GoodsReceiptItem saved = itemRepo.save(item);
        recomputeReceiptStatus(receipt);
        return saved;
    }

    @Transactional
    public GoodsReceiptItem updateItem(Long itemId,
                                       Integer actualQty,
                                       String defectNotes) {
        GoodsReceiptItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item " + itemId + " nicht gefunden"));

        if (actualQty != null) {
            item.setActualQuantity(actualQty);
        }
        if (defectNotes != null) {
            item.setDefectNotes(defectNotes);
        }

        GoodsReceiptItem saved = itemRepo.save(item);
        recomputeReceiptStatus(saved.getGoodsReceipt());
        return saved;
    }

    @Transactional
    public GoodsReceiptItem setItemStatus(Long itemId, GoodsReceiptItemStatus status) {
        GoodsReceiptItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item " + itemId + " nicht gefunden"));

        // Idempotent: bereits im Zielstatus -> kein Update noetig
        if (item.getStatus() == status) {
            return item;
        }

        System.out.println("[setItemStatus] itemId=" + itemId + " status=" + status + " receiptId=" + item.getGoodsReceipt().getId());
        item.setStatus(status);
        GoodsReceiptItem saved = itemRepo.save(item);

        // Gesamtstatus des Wareneingangs anpassen
        recomputeReceiptStatus(saved.getGoodsReceipt());

        return saved;
    }

    /**
     * Bulk-Freigabe aller IN_PRUEFUNG-Items eines Wareneingangs in 2 Queries.
     * Query 1: UPDATE goods_receipt_item SET status=FREIGEGEBEN WHERE receipt_id=? AND status=IN_PRUEFUNG
     * Query 2: findByGoodsReceiptId + bedingtes receiptRepo.save in recomputeReceiptStatus
     */
    @Transactional
    public int approveAllItemsForReceipt(Long receiptId) {
        int updated = itemRepo.updateStatusByReceiptId(
                receiptId,
                GoodsReceiptItemStatus.FREIGEGEBEN,
                GoodsReceiptItemStatus.IN_PRUEFUNG);
        GoodsReceipt receipt = getById(receiptId);
        recomputeReceiptStatus(receipt);
        return updated;
    }

    // ------------------------------------------------------------------------
    // Abschluss der Prüfung
    // ------------------------------------------------------------------------

    /**
     * Schließt die Prüfung eines Wareneingangs ab:
     * - Wenn noch Items IN_PRUEFUNG sind -> Exception (Prüfung nicht vollständig)
     * - Wenn alle Items FREIGEGEBEN -> Wareneingang = FREIGEGEBEN
     * - Sonst -> Wareneingang = GEPRUEFT
     *
     * Zusätzlich:
     * - Für alle FREIGEGEBENEN Positionen wird die Ist-Menge
     *   auf ArticleInfo.reservePallets addiert.
     */
    @Transactional
    public GoodsReceipt completeInspection(Long receiptId) {
        GoodsReceipt gr = getById(receiptId);

        System.out.println("[completeInspection] receiptId=" + receiptId + " currentStatus=" + gr.getStatus());

        if (gr.getStatus() == GoodsReceiptStatus.FREIGEGEBEN) {
            return gr; // idempotent: bereits vollstaendig abgeschlossen
        }

        List<GoodsReceiptItem> items = getItemsForReceipt(receiptId);
        System.out.println("[completeInspection] items=" + items.size() + " statuses=" + items.stream().map(i -> i.getStatus().name()).toList());

        boolean anyInPruefung = items.stream()
                .anyMatch(i -> i.getStatus() == GoodsReceiptItemStatus.IN_PRUEFUNG);

        if (anyInPruefung) {
            System.out.println("[completeInspection] BLOCKED – noch IN_PRUEFUNG");
            throw new IllegalStateException(
                    "Prüfung kann nicht abgeschlossen werden: es gibt noch Positionen IN_PRUEFUNG");
        }

        // 1) freigegebene Positionen in reserve_pallets übertragen
        applyApprovedItemsToReserve(receiptId);

        // 2) Status des Wareneingangs setzen
        boolean allFreigegeben = !items.isEmpty() && items.stream()
                .allMatch(i -> i.getStatus() == GoodsReceiptItemStatus.FREIGEGEBEN);

        if (allFreigegeben) {
            gr.setStatus(GoodsReceiptStatus.FREIGEGEBEN);
        } else {
            gr.setStatus(GoodsReceiptStatus.GEPRUEFT);
        }

        return receiptRepo.save(gr);
    }

    // ------------------------------------------------------------------------
    // Hilfslogik: Status vom Wareneingang aus Items ableiten
    // ------------------------------------------------------------------------

    private void recomputeReceiptStatus(GoodsReceipt receipt) {
        List<GoodsReceiptItem> items = itemRepo.findByGoodsReceiptId(receipt.getId());

        GoodsReceiptStatus newStatus;
        if (items.isEmpty()) {
            newStatus = GoodsReceiptStatus.IN_PRUEFUNG;
        } else {
            boolean anyInPruefung = items.stream()
                    .anyMatch(i -> i.getStatus() == GoodsReceiptItemStatus.IN_PRUEFUNG);
            // FREIGEGEBEN wird ausschliesslich durch completeInspection gesetzt,
            // weil dort auch applyApprovedItemsToReserve ausgefuehrt wird.
            // Hier nur: noch offen (IN_PRUEFUNG) oder alle entschieden (GEPRUEFT).
            newStatus = anyInPruefung ? GoodsReceiptStatus.IN_PRUEFUNG : GoodsReceiptStatus.GEPRUEFT;
        }

        // Kein Save wenn sich der Status nicht geaendert hat: verhindert
        // unnoetige version-Bumps und reduziert Optimistic-Locking-Konflikte
        // bei parallelen setItemStatus-Aufrufen auf demselben Wareneingang.
        if (receipt.getStatus() == newStatus) {
            return;
        }

        receipt.setStatus(newStatus);
        receiptRepo.save(receipt);
    }

    // ------------------------------------------------------------------------
    // Reservelogik: freigegebene Mengen -> ArticleInfo.reservePallets
    // ------------------------------------------------------------------------

    @Transactional
    protected void applyApprovedItemsToReserve(Long receiptId) {
        List<GoodsReceiptItem> items = itemRepo.findByGoodsReceiptId(receiptId);

        List<ArticleInfo> articlesToSave = new ArrayList<>();
        for (GoodsReceiptItem item : items) {
            if (item.getStatus() == GoodsReceiptItemStatus.FREIGEGEBEN) {
                ArticleInfo article = item.getArticle();
                if (article != null) {
                    int addQty = item.getActualQuantity() != null ? item.getActualQuantity() : 0;
                    int oldReserve = article.getReservePallets() != null ? article.getReservePallets() : 0;
                    article.setReservePallets(oldReserve + addQty);
                    articlesToSave.add(article);
                }
            }
        }
        articleRepo.saveAll(articlesToSave);
    }
}




