package com.example.application.services;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.stockChangeLog.ChangeType;
import com.example.application.data.stockChangeLog.StockChangeLog;
import com.example.application.data.stockChangeLog.StockChangeLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleInfoServiceTest {

    @Mock
    private ArticleInfoRepository articleInfoRepository;
    @Mock
    private StorageLocationService storageLocationService;
    @Mock
    private StockChangeLogRepository logRepository;

    @InjectMocks
    private ArticleInfoService service;

    // -------------------------------------------------------------------------
    // Hilfsmethode: ArticleInfo für Tests aufbauen
    // -------------------------------------------------------------------------

    private ArticleInfo makeArticle(int stock, int piecesPerPallet, int reservePallets) {
        ArticleInfo a = new ArticleInfo();
        a.setId(1L);
        a.setArticleId(1L);
        a.setArticleNumber("ART-1");
        a.setName("Testartikel");
        a.setStorageLocation("Z1.S1.C1");
        a.setStockLevel(stock);
        a.setPiecesPerPallet(piecesPerPallet);
        a.setReservePallets(reservePallets);
        return a;
    }

    // =========================================================================
    // updateStock – Bestand beim Kommissionieren abziehen
    // =========================================================================

    @Test
    void updateStock_subtractsNormally_whenStockSufficient() {
        ArticleInfo article = makeArticle(10, 5, 2);
        when(articleInfoRepository.findByArticleNumber("ART-1")).thenReturn(article);
        when(articleInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boolean result = service.updateStock("ART-1", 3);

        assertThat(result).isTrue();
        assertThat(article.getStockLevel()).isEqualTo(7);
        assertThat(article.getReservePallets()).isEqualTo(2); // Reserve unberührt
    }

    @Test
    void updateStock_depletesFully_whenChangeEqualsStock() {
        ArticleInfo article = makeArticle(10, 5, 0);
        when(articleInfoRepository.findByArticleNumber("ART-1")).thenReturn(article);
        when(articleInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boolean result = service.updateStock("ART-1", 10);

        assertThat(result).isTrue();
        assertThat(article.getStockLevel()).isEqualTo(0);
    }

    @Test
    void updateStock_returnsFalse_whenArticleNotFound() {
        when(articleInfoRepository.findByArticleNumber("UNBEKANNT")).thenReturn(null);

        boolean result = service.updateStock("UNBEKANNT", 5);

        assertThat(result).isFalse();
        verify(articleInfoRepository, never()).save(any());
    }

    @Test
    void updateStock_returnsFalse_whenStockInsufficient_andNoReserve() {
        ArticleInfo article = makeArticle(2, 5, 0);
        when(articleInfoRepository.findByArticleNumber("ART-1")).thenReturn(article);

        boolean result = service.updateStock("ART-1", 10);

        assertThat(result).isFalse();
        assertThat(article.getStockLevel()).isEqualTo(2); // unverändert
        verify(articleInfoRepository, never()).save(any());
    }

    @Test
    void updateStock_opensPallet_whenStockEmpty_butReserveAvailable() {
        // Genau 1 Palette nötig: ppp=10, change=5 → Math.abs(10-5)=5 → setStock(10-5=5)
        ArticleInfo article = makeArticle(0, 10, 1);
        when(articleInfoRepository.findByArticleNumber("ART-1")).thenReturn(article);
        when(articleInfoRepository.findByArticleId(1L)).thenReturn(article);
        when(articleInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boolean result = service.updateStock("ART-1", 5);

        assertThat(result).isTrue();
        assertThat(article.getReservePallets()).isEqualTo(0); // Palette wurde geöffnet
    }

    // =========================================================================
    // applyStockChange – manueller Lagerausgleich (mit Log-Eintrag)
    // =========================================================================

    @Test
    void applyStockChange_addsStock_andWritesLogEntry() {
        ArticleInfo article = makeArticle(10, 5, 1);
        when(articleInfoRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.applyStockChange(article, 5, ChangeType.RECEIPT, "Eingang", "Nutzer1");

        assertThat(article.getStockLevel()).isEqualTo(15);

        ArgumentCaptor<StockChangeLog> logCaptor = ArgumentCaptor.forClass(StockChangeLog.class);
        verify(logRepository).save(logCaptor.capture());
        StockChangeLog log = logCaptor.getValue();
        assertThat(log.getOldStock()).isEqualTo(10);
        assertThat(log.getDelta()).isEqualTo(5);
        assertThat(log.getNewStock()).isEqualTo(15);
        assertThat(log.getChangeType()).isEqualTo(ChangeType.RECEIPT);
        assertThat(log.getChangedBy()).isEqualTo("Nutzer1");
    }

    @Test
    void applyStockChange_subtractsStock_whenSufficient() {
        ArticleInfo article = makeArticle(10, 5, 0);
        when(articleInfoRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.applyStockChange(article, -3, ChangeType.ISSUE, null, null);

        assertThat(article.getStockLevel()).isEqualTo(7);
    }

    @Test
    void applyStockChange_cappedAtZero_whenNoReserveAvailable() {
        // delta so groß, dass Bestand negativ wäre – soll bei 0 stoppen
        ArticleInfo article = makeArticle(3, 0, 0);
        when(articleInfoRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.applyStockChange(article, -10, ChangeType.ISSUE, null, null);

        assertThat(article.getStockLevel()).isEqualTo(0);
    }

    @Test
    void applyStockChange_opensPallet_whenNegativeDeltaDrainsStock() {
        // stock=0, delta=-3, ppp=5, reserve=2
        // newStock = 0+(-3) = -3 → Palette öffnen: -3+5=2, reserve=1
        ArticleInfo article = makeArticle(0, 5, 2);
        when(articleInfoRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.applyStockChange(article, -3, ChangeType.ISSUE, null, null);

        assertThat(article.getStockLevel()).isEqualTo(2);
        assertThat(article.getReservePallets()).isEqualTo(1);
    }

    @Test
    void applyStockChange_logEntryReflectsFinalStock_afterPalletOpening() {
        // Log soll den Endbestand nach Palettenöffnung enthalten
        ArticleInfo article = makeArticle(0, 5, 1);
        when(articleInfoRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleInfoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.applyStockChange(article, -2, ChangeType.ISSUE, "Entnahme", null);

        ArgumentCaptor<StockChangeLog> logCaptor = ArgumentCaptor.forClass(StockChangeLog.class);
        verify(logRepository).save(logCaptor.capture());
        StockChangeLog log = logCaptor.getValue();
        assertThat(log.getOldStock()).isEqualTo(0);
        assertThat(log.getDelta()).isEqualTo(-2);
        // newStock nach Palette: 0+(-2)=-2 → Palette: -2+5=3 → newStock=3
        assertThat(log.getNewStock()).isEqualTo(3);
    }
}
