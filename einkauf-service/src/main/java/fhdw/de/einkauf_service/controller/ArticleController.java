package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import fhdw.de.einkauf_service.dto.ArticleRequestDTO;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;
import fhdw.de.einkauf_service.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // ==================================================================================
    // 1. CREATE (POST)
    // Erwartet ArticleRequest, gibt ArticleResponse zurück
    // ==================================================================================
    @PostMapping
    public ResponseEntity<ArticleResponseDTO> createArticle(@Valid @RequestBody ArticleRequestDTO articleRequestDto) {
        try {
            ArticleResponseDTO createdArticle = articleService.createNewArticle(articleRequestDto);
            // HTTP 201 Created ist Standard für erfolgreiches Anlegen
            return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Fängt die Duplikat-Prüfung aus dem Service ab
            return new ResponseEntity<>(null, HttpStatus.CONFLICT); // 409 Conflict
        }
    }

    // ==================================================================================
    // 2. READ by filter (GET)
    // Gibt eine Liste von ArticleResponse zurück
    // ==================================================================================
    @GetMapping
    public ResponseEntity<List<ArticleResponseDTO>> searchArticles(ArticleFilterDTO filter) {

        // 1. Service aufrufen: Der Service liefert bereits die gemappten DTOs
        List<ArticleResponseDTO> responseDTOs = articleService.findFilteredArticles(filter);

        // 2. Antwort zurückgeben (direkt)
        return ResponseEntity.ok(responseDTOs);
    }

    // ==================================================================================
    // 3. READ BY ID (GET /{id})
    // Gibt ArticleResponse zurück
    // ==================================================================================
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponseDTO> getArticleById(@PathVariable Long id) {
        try {
            ArticleResponseDTO article = articleService.findArticleById(id);
            return ResponseEntity.ok(article); // 200 OK
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    // ==================================================================================
    // 4. UPDATE (PUT /{id})
    // Erwartet ArticleRequest, gibt ArticleResponse zurück
    // ==================================================================================
    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponseDTO> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequestDTO articleDetails) {
        try {
            ArticleResponseDTO updatedArticle = articleService.updateArticle(id, articleDetails);
            return ResponseEntity.ok(updatedArticle); // 200 OK
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    // ==================================================================================
    // 5. DELETE (DELETE /{id})
    // Gibt 204 No Content zurück
    // ==================================================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        try {
            articleService.deleteArticle(id);
            // HTTP 204 No Content ist Standard für erfolgreiches Löschen ohne Rückgabe-Body
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }
}