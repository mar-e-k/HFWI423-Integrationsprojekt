package com.example.application.services;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.goodsreceipts.*;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.data.restockorder.RestockOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptServiceTest {

    @Mock
    private GoodsReceiptRepository receiptRepo;
    @Mock
    private GoodsReceiptItemRepository itemRepo;
    @Mock
    private ArticleInfoRepository articleRepo;
    @Mock
    private RestockOrderRepository restockOrderRepo;
    @Mock
    private JdbcTemplate jdbc;

    @InjectMocks
    private GoodsReceiptService service;

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private GoodsReceipt makeReceipt(Long id, GoodsReceiptStatus status) {
        GoodsReceipt r = new GoodsReceipt();
        r.setId(id);
        r.setReceiptNumber("WE-2026-00001");
        r.setSupplierName("Lieferant AG");
        r.setDeliveryNoteNumber("LS-001");
        r.setDeliveryDate(LocalDate.now());
        r.setStatus(status);
        return r;
    }

    private ArticleInfo makeArticle(String articleNumber, int piecesPerPallet, int reservePallets) {
        ArticleInfo a = new ArticleInfo();
        a.setId(1L);
        a.setArticleId(1L);
        a.setArticleNumber(articleNumber);
        a.setName("Testartikel");
        a.setStorageLocation("Z1.S1.C1");
        a.setStockLevel(0);
        a.setPiecesPerPallet(piecesPerPallet);
        a.setReservePallets(reservePallets);
        return a;
    }

    private GoodsReceiptItem makeItem(GoodsReceipt receipt, ArticleInfo article,
                                      int actualQty, GoodsReceiptItemStatus status) {
        GoodsReceiptItem item = new GoodsReceiptItem();
        item.setGoodsReceipt(receipt);
        item.setArticle(article);
        item.setExpectedQuantity(actualQty);
        item.setActualQuantity(actualQty);
        item.setStatus(status);
        return item;
    }

    // =========================================================================
    // completeInspection – Prüfung abschließen
    // =========================================================================

    @Test
    void completeInspection_allFreigegeben_setsStatusFreigegeben_andAddsToReserve() {
        GoodsReceipt receipt = makeReceipt(10L, GoodsReceiptStatus.IN_PRUEFUNG);
        ArticleInfo article = makeArticle("ART-1", 5, 0);
        GoodsReceiptItem item = makeItem(receipt, article, 3, GoodsReceiptItemStatus.FREIGEGEBEN);

        when(receiptRepo.findById(10L)).thenReturn(Optional.of(receipt));
        when(itemRepo.findByGoodsReceiptId(10L)).thenReturn(List.of(item));
        when(receiptRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GoodsReceipt result = service.completeInspection(10L);

        assertThat(result.getStatus()).isEqualTo(GoodsReceiptStatus.FREIGEGEBEN);
        assertThat(article.getReservePallets()).isEqualTo(3); // 0 + 3 freigegebene Paletten
        verify(articleRepo).save(article);
    }

    @Test
    void completeInspection_mixedItems_setsStatusGeprueft_addsOnlyFreigegebene() {
        GoodsReceipt receipt = makeReceipt(10L, GoodsReceiptStatus.IN_PRUEFUNG);
        ArticleInfo articleA = makeArticle("ART-A", 5, 0);
        ArticleInfo articleB = makeArticle("ART-B", 5, 0);
        GoodsReceiptItem freigegeben = makeItem(receipt, articleA, 2, GoodsReceiptItemStatus.FREIGEGEBEN);
        GoodsReceiptItem gesperrt   = makeItem(receipt, articleB, 1, GoodsReceiptItemStatus.GESPERRT);

        when(receiptRepo.findById(10L)).thenReturn(Optional.of(receipt));
        when(itemRepo.findByGoodsReceiptId(10L)).thenReturn(List.of(freigegeben, gesperrt));
        when(receiptRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GoodsReceipt result = service.completeInspection(10L);

        assertThat(result.getStatus()).isEqualTo(GoodsReceiptStatus.GEPRUEFT);
        assertThat(articleA.getReservePallets()).isEqualTo(2); // nur freigegebene
        assertThat(articleB.getReservePallets()).isEqualTo(0); // gesperrte unberührt
    }

    @Test
    void completeInspection_throwsException_whenItemStillInPruefung() {
        GoodsReceipt receipt = makeReceipt(10L, GoodsReceiptStatus.IN_PRUEFUNG);
        ArticleInfo article = makeArticle("ART-1", 5, 0);
        GoodsReceiptItem item = makeItem(receipt, article, 2, GoodsReceiptItemStatus.IN_PRUEFUNG);

        when(receiptRepo.findById(10L)).thenReturn(Optional.of(receipt));
        when(itemRepo.findByGoodsReceiptId(10L)).thenReturn(List.of(item));

        assertThatThrownBy(() -> service.completeInspection(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("IN_PRUEFUNG");
    }

    @Test
    void completeInspection_addsToExistingReserve_notReplacing() {
        // Artikel hat bereits 4 Reservepaletten – neue kommen obendrauf
        GoodsReceipt receipt = makeReceipt(10L, GoodsReceiptStatus.IN_PRUEFUNG);
        ArticleInfo article = makeArticle("ART-1", 5, 4);
        GoodsReceiptItem item = makeItem(receipt, article, 3, GoodsReceiptItemStatus.FREIGEGEBEN);

        when(receiptRepo.findById(10L)).thenReturn(Optional.of(receipt));
        when(itemRepo.findByGoodsReceiptId(10L)).thenReturn(List.of(item));
        when(receiptRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.completeInspection(10L);

        assertThat(article.getReservePallets()).isEqualTo(7); // 4 + 3
    }

    // =========================================================================
    // createFromRestockOrders – Paletten-Berechnung
    // =========================================================================

    @Test
    void createFromRestockOrders_calculatesPalletsCorrectly() {
        // 10 Stück / 2 ppp = 5 Paletten
        setupCreateFromRestockOrders("ART-1", 10, 2);

        service.createFromRestockOrders(List.of(1L), "Lieferant", "LS-001", LocalDate.now());

        ArgumentCaptor<GoodsReceiptItem> itemCaptor = ArgumentCaptor.forClass(GoodsReceiptItem.class);
        verify(itemRepo).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getExpectedQuantity()).isEqualTo(5);
        assertThat(itemCaptor.getValue().getActualQuantity()).isEqualTo(5);
    }

    @Test
    void createFromRestockOrders_ignoresRemainder_whenPiecesNotDivisible() {
        // 11 Stück / 2 ppp = 5 Paletten (Rest wird ignoriert)
        setupCreateFromRestockOrders("ART-1", 11, 2);

        service.createFromRestockOrders(List.of(1L), "Lieferant", "LS-001", LocalDate.now());

        ArgumentCaptor<GoodsReceiptItem> itemCaptor = ArgumentCaptor.forClass(GoodsReceiptItem.class);
        verify(itemRepo).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getExpectedQuantity()).isEqualTo(5);
    }

    @Test
    void createFromRestockOrders_throwsException_whenPiecesPerPalletMissing() {
        RestockOrder ro = new RestockOrder();
        ro.setId(1L);
        ro.setArticleNumber("ART-1");
        ro.setQuantity(10);

        ArticleInfo article = makeArticle("ART-1", 0, 0); // piecesPerPallet=0 → ungültig

        GoodsReceipt savedReceipt = makeReceipt(99L, GoodsReceiptStatus.IN_PRUEFUNG);
        doReturn(1L).when(jdbc).queryForObject(anyString(), eq(Long.class));
        when(receiptRepo.save(any())).thenReturn(savedReceipt);
        when(restockOrderRepo.findById(1L)).thenReturn(Optional.of(ro));
        when(articleRepo.findByArticleNumber("ART-1")).thenReturn(article);

        assertThatThrownBy(() ->
                service.createFromRestockOrders(List.of(1L), "Lieferant", "LS-001", LocalDate.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pieces_per_pallet");
    }

    // -------------------------------------------------------------------------
    // Hilfsmethode für createFromRestockOrders-Tests
    // -------------------------------------------------------------------------

    private void setupCreateFromRestockOrders(String articleNumber, int pieces, int piecesPerPallet) {
        RestockOrder ro = new RestockOrder();
        ro.setId(1L);
        ro.setArticleNumber(articleNumber);
        ro.setQuantity(pieces);

        ArticleInfo article = makeArticle(articleNumber, piecesPerPallet, 0);
        GoodsReceipt savedReceipt = makeReceipt(99L, GoodsReceiptStatus.IN_PRUEFUNG);

        doReturn(1L).when(jdbc).queryForObject(anyString(), eq(Long.class));
        when(receiptRepo.save(any())).thenReturn(savedReceipt);
        when(restockOrderRepo.findById(1L)).thenReturn(Optional.of(ro));
        when(articleRepo.findByArticleNumber(articleNumber)).thenReturn(article);
        when(itemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(itemRepo.findByGoodsReceiptId(99L)).thenReturn(List.of());
        when(restockOrderRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }
}
