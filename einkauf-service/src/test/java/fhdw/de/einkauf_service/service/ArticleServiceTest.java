package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.dto.ArticleRequestDTO;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.serviceImpl.ArticleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Aktiviert die Mockito-Erweiterung für JUnit 5
public class ArticleServiceTest {

    // 1. Abhängigkeit MOCKEN (simulieren)
    @Mock
    private ArticleRepository articleRepository;

    // 2. Das Objekt, das getestet wird, mit den Mocks 'injizieren'
    @InjectMocks
    private ArticleServiceImpl articleServiceImpl;

    // Testdaten (DTO/Entity)
    private ArticleRequestDTO validRequest;
    private Article savedEntity; // Entity, wie sie nach dem Speichern zurückkommen würde

    // Wird vor jedem Test ausgeführt
    @BeforeEach
    void setUp() {
        // Beispiel-Request-DTO erstellen
        validRequest = new ArticleRequestDTO(
                "4008400403337", "Test Schokoriegel", "STK",
                10.00, 19.0, "Hersteller X", "Lieferant Y", 100, "Beschreibung"
        );

        // Entität, die das Repository nach dem Speichern zurückgeben würde
        savedEntity = new Article();
        savedEntity.setId(1L);
        savedEntity.setArticleNumber(validRequest.getArticleNumber());
        savedEntity.setPurchasePrice(10.00);
        savedEntity.setTaxRatePercent(19.0);
        // Hinweis: Der SellingPrice wird im Service berechnet und in die Entity gesetzt.
        savedEntity.setSellingPrice(11.90);
    }



    @Test
    void shouldCalculateSellingPriceCorrectly() {
        // ARRANGE: Wie soll das Mock-Repository reagieren?
        // 1. Wenn der Service die Duplikatprüfung macht (findByArticleNumber), soll es leer sein.
        when(articleRepository.findByArticleNumber(anyString())).thenReturn(Optional.empty());
        // 2. Wenn der Service die Speicherung aufruft, gib die vorbereitete Entity zurück.
        when(articleRepository.save(any(Article.class))).thenReturn(savedEntity);

        // ACT: Die Methode ausführen, die getestet werden soll
        var response = articleServiceImpl.createNewArticle(validRequest);

        // ASSERT: Ergebnisse überprüfen
        // 1. Prüfen, ob der Verkaufspreis korrekt ist (10.00 * 1.19 = 11.90)
        assertEquals(11.90, response.getSellingPrice(), 0.001); // 0.001 ist die erlaubte Abweichung
        // 2. Prüfen, ob die Speichermethode tatsächlich aufgerufen wurde
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    @Test
    void shouldThrowExceptionOnDuplicateArticleNumber() {
        // ARRANGE: Wie soll das Mock-Repository reagieren?
        // Wenn der Service die Duplikatprüfung macht, soll es EINE Entity zurückgeben (Duplikat gefunden).
        when(articleRepository.findByArticleNumber(anyString())).thenReturn(Optional.of(savedEntity));

        // ACT & ASSERT: Prüfen, ob die erwartete Exception geworfen wird
        // Beim Aufruf der Methode sollte die IllegalArgumentException geworfen werden
        assertThrows(IllegalArgumentException.class, () -> {
            articleServiceImpl.createNewArticle(validRequest);
        });

        // Prüfen, ob die Speichermethode NICHT aufgerufen wurde
        verify(articleRepository, never()).save(any(Article.class));
    }

    /**
     * Testfälle für Update (PUT)
     */
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingArticle() {
        // ARRANGE: Mockito soll eine leere Antwort liefern, wenn die ID gesucht wird (Artikel existiert nicht)
        when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // ACT & ASSERT: Prüfen, ob die NoSuchElementException geworfen wird
        assertThrows(NoSuchElementException.class, () -> {
            articleServiceImpl.updateArticle(99L, validRequest); // 99L ist eine nicht existierende ID
        });

        // ASSERT: Prüfen, ob die Speicherung niemals aufgerufen wurde
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void shouldRecalculateSellingPriceOnUpdate() {
        // ARRANGE: Setze den neuen Einkaufspreis im Request für den Update-Test
        ArticleRequestDTO updateRequest = new ArticleRequestDTO(
                "4008400403337", "Geänderter Name", "STK",
                20.00, 10.0, "Hersteller X", "Lieferant Y", 100, "Beschreibung"
        );

        // Die Entity, die das Mock-Repository beim findById zurückgibt (alter Zustand)
        Article existingArticle = savedEntity;
        existingArticle.setSellingPrice(11.90); // Alter Preis

        // Die Entity, die das Repository nach save zurückgibt (neuer Zustand)
        Article updatedArticle = existingArticle;
        updatedArticle.setPurchasePrice(20.00);
        updatedArticle.setTaxRatePercent(10.0);

        // Mocks konfigurieren
        when(articleRepository.findById(1L)).thenReturn(Optional.of(existingArticle));
        when(articleRepository.save(any(Article.class))).thenReturn(updatedArticle);

        // ACT
        var response = articleServiceImpl.updateArticle(1L, updateRequest);

        // ASSERT: Prüfen, ob der neue Verkaufspreis korrekt ist (20.00 * 1.10 = 22.00)
        assertEquals(22.00, response.getSellingPrice(), 0.001);
        assertEquals("Geänderter Name", response.getName());
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    /**
     * Testfälle für DELETE
     */
    @Test
    void shouldDeleteArticleSuccessfully() {
        // ARRANGE: Mockito soll bestätigen, dass die ID existiert
        when(articleRepository.existsById(1L)).thenReturn(true);

        // ACT
        articleServiceImpl.deleteArticle(1L);

        // ASSERT: Prüfen, ob die deleteById-Methode exakt einmal aufgerufen wurde
        verify(articleRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingArticle() {
        // ARRANGE: Mockito soll mitteilen, dass die ID nicht existiert
        when(articleRepository.existsById(99L)).thenReturn(false);

        // ACT & ASSERT: Prüfen, ob die NoSuchElementException geworfen wird
        assertThrows(NoSuchElementException.class, () -> {
            articleServiceImpl.deleteArticle(99L);
        });

        // ASSERT: Prüfen, ob die Löschmethode niemals aufgerufen wurde
        verify(articleRepository, never()).deleteById(anyLong());
    }
}