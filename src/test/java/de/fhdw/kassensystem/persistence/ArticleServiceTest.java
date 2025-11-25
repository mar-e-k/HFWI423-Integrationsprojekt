package de.fhdw.kassensystem.persistence;

import de.fhdw.kassensystem.persistence.entity.imported.Article;
import de.fhdw.kassensystem.persistence.repository.ArticleRepository;
import de.fhdw.kassensystem.persistence.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArticleServiceTest {

    /**
     * SCRUM-39 – Artikeldaten abrufen:
     * Prüft, dass bei gültiger Artikelnummer der Artikel korrekt aus dem Repository
     * geladen und mit Name und Verkaufspreis zurückgegeben wird.
     */
    @Test
    void findByArticleNumber_returnsArticleForValidNumber() {
        ArticleRepository repo = Mockito.mock(ArticleRepository.class);
        ArticleService service = new ArticleService(repo);

        Article article = new Article();
        article.setArticleNumber("A-123");
        article.setName("Scanner-Artikel");
        article.setSellingPrice(1.23);
        article.setTaxRatePercent(19.0);
        article.setIsAvailable(true);

        when(repo.findByArticleNumber("A-123")).thenReturn(Optional.of(article));

        Optional<Article> result = service.findByArticleNumber("A-123");

        assertAll(
                () -> assertTrue(result.isPresent(),
                        "Für eine gültige Artikelnummer muss ein Artikel gefunden werden."),
                () -> assertEquals("Scanner-Artikel", result.get().getName(),
                        "Artikelname des gefundenen Artikels ist unerwartet."),
                () -> assertEquals(1.23, result.get().getSellingPrice(), 0.0001,
                        "Verkaufspreis des gefundenen Artikels ist unerwartet."),
                () -> assertSame(article, result.get(),
                        "ArticleService sollte genau den vom Repository gelieferten Artikel zurückgeben.")
        );

        verify(repo, times(1)).findByArticleNumber("A-123");
        verifyNoMoreInteractions(repo);
    }

    /**
     * SCRUM-39 – Artikeldaten abrufen:
     * Stellt sicher, dass bei einer unbekannten Artikelnummer kein Artikel gefunden
     * wird und der Service Optional.empty() zurückliefert – Basis für eine klare
     * Fehlermeldung in der Oberfläche.
     */
    @Test
    void findByArticleNumber_returnsEmptyForUnknownNumber() {
        ArticleRepository repo = Mockito.mock(ArticleRepository.class);
        ArticleService service = new ArticleService(repo);

        when(repo.findByArticleNumber("A-999")).thenReturn(Optional.empty());

        Optional<Article> result = service.findByArticleNumber("A-999");

        assertTrue(result.isEmpty(),
                "Für eine unbekannte Artikelnummer darf kein Artikel zurückgegeben werden.");

        verify(repo, times(1)).findByArticleNumber("A-999");
        verifyNoMoreInteractions(repo);
    }
}