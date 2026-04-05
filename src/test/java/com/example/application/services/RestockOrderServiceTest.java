package com.example.application.services;

import com.example.application.amqp.einkaufEvents.EinkaufEventPublisher;
import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.RestockItem;
import com.example.application.data.contingent.Contingent;
import com.example.application.data.contingent.ContingentRepository;
import com.example.application.data.restockorder.RestockOrder;
import com.example.application.data.restockorder.RestockOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestockOrderServiceTest {

    @Mock
    private RestockOrderRepository restockOrderRepository;
    @Mock
    private ContingentRepository contingentRepository;
    @Mock
    private EinkaufEventPublisher einkaufEventPublisher;

    @InjectMocks
    private RestockOrderService service;

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private ArticleInfo makeArticle(String articleNumber, int piecesPerPallet, int minStock, int reservePallets) {
        ArticleInfo a = new ArticleInfo();
        a.setId(1L);
        a.setArticleId(1L);
        a.setArticleNumber(articleNumber);
        a.setName("Testartikel");
        a.setStorageLocation("Z1.S1.C1");
        a.setStockLevel(5);
        a.setPiecesPerPallet(piecesPerPallet);
        a.setMinStock(minStock);
        a.setReservePallets(reservePallets);
        return a;
    }

    private Contingent makeContingent(Long id, Long articleId, int availableQuantity) {
        Contingent c = new Contingent();
        try {
            Field idField = Contingent.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(c, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        c.setArticleId(articleId);
        c.setAvailableQuantity(availableQuantity);
        return c;
    }

    // =========================================================================
    // approveOrder – Kontingent-Abzug und RestockOrder-Anlage
    // =========================================================================

    @Test
    void approveOrder_subtractsCorrectly_fromSingleContingent() {
        // 2 Paletten × 10 ppp = 20 Stück werden bestellt
        ArticleInfo article = makeArticle("ART-1", 10, 5, 3);
        RestockItem item = new RestockItem(article); // orderAmount = (5*2) - 3 = 7 Paletten
        Contingent contingent = makeContingent(1L, 1L, 500);

        when(contingentRepository.sumAvailableQuantityByArticleId(1L)).thenReturn(500, 430);
        when(contingentRepository.findAllByArticleId(1L)).thenReturn(new ArrayList<>(List.of(contingent)));
        when(contingentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(restockOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveOrder(item);

        // 7 Paletten × 10 ppp = 70 Stück abgezogen
        assertThat(contingent.getAvailableQuantity()).isEqualTo(430); // 500 - 70
    }

    @Test
    void approveOrder_subtractsAcrossMultipleContingents_fifo() {
        // 3 Paletten × 5 ppp = 15 Stück – verteilt auf 2 Kontingente
        ArticleInfo article = makeArticle("ART-1", 5, 2, 1);
        RestockItem item = new RestockItem(article); // orderAmount = (2*2) - 1 = 3 Paletten
        Contingent c1 = makeContingent(1L, 1L, 10);  // wird komplett geleert
        Contingent c2 = makeContingent(2L, 1L, 20);  // nimmt den Rest auf

        when(contingentRepository.sumAvailableQuantityByArticleId(1L)).thenReturn(30, 15);
        when(contingentRepository.findAllByArticleId(1L)).thenReturn(new ArrayList<>(List.of(c1, c2)));
        when(contingentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(restockOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveOrder(item);

        assertThat(c1.getAvailableQuantity()).isEqualTo(0);  // komplett verbraucht
        assertThat(c2.getAvailableQuantity()).isEqualTo(15); // 20 - 5 (Rest)
    }

    @Test
    void approveOrder_throwsException_whenContingentInsufficient() {
        ArticleInfo article = makeArticle("ART-1", 10, 5, 0);
        RestockItem item = new RestockItem(article); // orderAmount = 10 Paletten = 100 Stück
        Contingent contingent = makeContingent(1L, 1L, 50); // nur 50 verfügbar

        when(contingentRepository.sumAvailableQuantityByArticleId(1L)).thenReturn(50);
        when(contingentRepository.findAllByArticleId(1L)).thenReturn(new ArrayList<>(List.of(contingent)));
        when(contingentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> service.approveOrder(item))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nicht genug Kontingent");
    }

    @Test
    void approveOrder_publishesNewDeal_whenContingentFallsBelowThreshold() {
        // Nach Bestellung fällt Kontingent unter 50 → NewDealEvent soll gesendet werden
        ArticleInfo article = makeArticle("ART-1", 10, 1, 0);
        RestockItem item = new RestockItem(article); // orderAmount = 2 Paletten = 20 Stück
        Contingent contingent = makeContingent(1L, 1L, 60);

        when(contingentRepository.sumAvailableQuantityByArticleId(1L)).thenReturn(60, 40); // nach Bestellung: 40 < 50
        when(contingentRepository.findAllByArticleId(1L)).thenReturn(new ArrayList<>(List.of(contingent)));
        when(contingentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(restockOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveOrder(item);

        verify(einkaufEventPublisher).publishNewDeal(1L);
    }

    @Test
    void approveOrder_doesNotPublishNewDeal_whenContingentStaysAboveThreshold() {
        // Nach Bestellung bleibt Kontingent über 50 → kein NewDealEvent
        ArticleInfo article = makeArticle("ART-1", 10, 1, 0);
        RestockItem item = new RestockItem(article); // orderAmount = 2 Paletten = 20 Stück
        Contingent contingent = makeContingent(1L, 1L, 200);

        when(contingentRepository.sumAvailableQuantityByArticleId(1L)).thenReturn(200, 180); // nach Bestellung: 180 >= 50
        when(contingentRepository.findAllByArticleId(1L)).thenReturn(new ArrayList<>(List.of(contingent)));
        when(contingentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(restockOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveOrder(item);

        verify(einkaufEventPublisher, never()).publishNewDeal(anyLong());
    }

    @Test
    void approveOrder_createsRestockOrder_withCorrectQuantityInPieces() {
        // Sicherstellen, dass die RestockOrder in STÜCK gespeichert wird (nicht Paletten)
        ArticleInfo article = makeArticle("ART-1", 10, 3, 0);
        RestockItem item = new RestockItem(article); // orderAmount = (3*2)-0 = 6 Paletten = 60 Stück
        Contingent contingent = makeContingent(1L, 1L, 500);

        when(contingentRepository.sumAvailableQuantityByArticleId(1L)).thenReturn(500, 440);
        when(contingentRepository.findAllByArticleId(1L)).thenReturn(new ArrayList<>(List.of(contingent)));
        when(contingentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(restockOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveOrder(item);

        ArgumentCaptor<RestockOrder> orderCaptor = ArgumentCaptor.forClass(RestockOrder.class);
        verify(restockOrderRepository).save(orderCaptor.capture());
        RestockOrder saved = orderCaptor.getValue();
        assertThat(saved.getQuantity()).isEqualTo(60); // 6 Paletten × 10 ppp
        assertThat(saved.getArticleNumber()).isEqualTo("ART-1");
        assertThat(saved.isApproved()).isTrue();
        assertThat(saved.isDelivered()).isFalse();
    }
}
