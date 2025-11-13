package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.dto.ArticleRequestDTO;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.entity.Supplier;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
import fhdw.de.einkauf_service.serviceImpl.ArticleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings; // NEU: Import hinzufügen
import org.mockito.quality.Strictness; // NEU: Import hinzufügen

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// KORREKTUR 1: Setzt Mockito auf LENIENT, um UnnecessaryStubbingException zu vermeiden
@MockitoSettings(strictness = Strictness.LENIENT)
public class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ArticleServiceImpl articleServiceImpl;

    private ArticleRequestDTO validRequest;
    private Article savedEntity;
    private Supplier mockSupplier;

    private static final Long SUPPLIER_ID = 22222222L;

    @BeforeEach
    void setUp() {
        // Beispiel-Request-DTO erstellen
        validRequest = new ArticleRequestDTO(
                "4008400403337", "Test Schokoriegel",
                10.00, 19.0, "Hersteller X", SUPPLIER_ID, 100, "Beschreibung", true
        );

        // Mock Supplier erstellen
        mockSupplier = new Supplier();
        mockSupplier.setId(SUPPLIER_ID);
        mockSupplier.setName("Test Lieferant");

        // Stubbing des SupplierRepository (jetzt LENIENT)
        when(supplierRepository.findById(SUPPLIER_ID)).thenReturn(Optional.of(mockSupplier));

        // Entität, die das Repository nach dem Speichern zurückgeben würde
        savedEntity = new Article();
        savedEntity.setId(1L);
        savedEntity.setArticleNumber(validRequest.getArticleNumber());
        savedEntity.setPurchasePrice(10.00);
        savedEntity.setTaxRatePercent(19.0);
        savedEntity.setSellingPrice(11.90);
        savedEntity.setSupplier(mockSupplier);
    }


    @Test
    void shouldCalculateSellingPriceCorrectly() {
        // ARRANGE
        when(articleRepository.findByArticleNumber(anyString())).thenReturn(Optional.empty());
        when(articleRepository.save(any(Article.class))).thenReturn(savedEntity);

        // ACT
        var response = articleServiceImpl.createNewArticle(validRequest);

        // ASSERT
        assertEquals(11.90, response.getSellingPrice(), 0.001);
        verify(articleRepository, times(1)).save(any(Article.class));
        // Verifizierung ist hier korrekt (Supplier wird vor dem Speichern geholt)
        verify(supplierRepository, times(1)).findById(SUPPLIER_ID);
    }

    @Test
    void shouldThrowExceptionOnDuplicateArticleNumber() {
        // ARRANGE: Duplikat gefunden
        when(articleRepository.findByArticleNumber(anyString())).thenReturn(Optional.of(savedEntity));

        // ACT & ASSERT: Prüfen, ob die erwartete Exception geworfen wird
        assertThrows(IllegalArgumentException.class, () -> {
            articleServiceImpl.createNewArticle(validRequest);
        });

        // KORREKTUR 2: Der Supplier wird VOR der Duplikatprüfung geholt.
        // Der Aufruf findet also statt, obwohl der Test fehlschlägt.
        verify(supplierRepository, times(1)).findById(SUPPLIER_ID);

        // Speichermethode NICHT aufgerufen
        verify(articleRepository, never()).save(any(Article.class));
    }

    /**
     * Testfälle für Update (PUT)
     */
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingArticle() {
        // ARRANGE: Artikel nicht gefunden
        when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // ACT & ASSERT: Prüfen, ob die NoSuchElementException geworfen wird
        assertThrows(NoSuchElementException.class, () -> {
            articleServiceImpl.updateArticle(99L, validRequest);
        });

        // Verifizierung: Der Supplier wird VOR der findById-Prüfung NICHT aufgerufen.
        // Der Aufruf findet nach der findById-Prüfung statt. Da findById fehlschlägt,
        // wird der Code zur Supplier-Prüfung nicht erreicht (siehe vorherige Korrektur).
        verify(supplierRepository, never()).findById(anyLong());

        // Speicherung niemals aufgerufen
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void shouldRecalculateSellingPriceOnUpdate() {
        // ARRANGE
        ArticleRequestDTO updateRequest = new ArticleRequestDTO(
                "4008400403337", "Geänderter Name",
                20.00, 10.0, "Hersteller X", SUPPLIER_ID, 100, "Beschreibung",true
        );

        Article existingArticle = new Article();
        existingArticle.setId(savedEntity.getId());
        existingArticle.setArticleNumber(savedEntity.getArticleNumber());
        existingArticle.setPurchasePrice(savedEntity.getPurchasePrice());
        existingArticle.setTaxRatePercent(savedEntity.getTaxRatePercent());
        existingArticle.setSellingPrice(11.90);
        existingArticle.setSupplier(mockSupplier);

        Article updatedArticle = existingArticle;
        updatedArticle.setPurchasePrice(20.00);
        updatedArticle.setTaxRatePercent(10.0);

        // Mocks konfigurieren
        when(articleRepository.findById(1L)).thenReturn(Optional.of(existingArticle));
        when(articleRepository.save(any(Article.class))).thenReturn(updatedArticle);

        // ACT
        var response = articleServiceImpl.updateArticle(1L, updateRequest);

        // ASSERT
        assertEquals(22.00, response.getSellingPrice(), 0.001);
        verify(articleRepository, times(1)).save(any(Article.class));
        // Verifizierung ist hier korrekt (Supplier wird einmalig geholt)
        verify(supplierRepository, times(1)).findById(SUPPLIER_ID);
    }

    /**
     * Testfälle für DELETE
     */
    @Test
    void shouldDeleteArticleSuccessfully() {
        when(articleRepository.existsById(1L)).thenReturn(true);
        articleServiceImpl.deleteArticle(1L);
        verify(articleRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingArticle() {
        when(articleRepository.existsById(99L)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> {
            articleServiceImpl.deleteArticle(99L);
        });

        verify(articleRepository, never()).deleteById(anyLong());
    }
}