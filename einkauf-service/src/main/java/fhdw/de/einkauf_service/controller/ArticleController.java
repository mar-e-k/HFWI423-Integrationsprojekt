package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.ArticleRequest;
import fhdw.de.einkauf_service.dto.ArticleResponse;
import fhdw.de.einkauf_service.service.ArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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
    public ResponseEntity<ArticleResponse> createArticle(@Valid @RequestBody ArticleRequest articleRequest) {
        try {
            ArticleResponse createdArticle = articleService.createNewArticle(articleRequest);
            // HTTP 201 Created ist Standard für erfolgreiches Anlegen
            return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Fängt die Duplikat-Prüfung aus dem Service ab
            return new ResponseEntity<>(null, HttpStatus.CONFLICT); // 409 Conflict
        }
    }

    // ==================================================================================
    // 2. READ ALL (GET)
    // Gibt eine Liste von ArticleResponse zurück
    // ==================================================================================
    @GetMapping
    public ResponseEntity<List<ArticleResponse>> getAllArticles() {
        List<ArticleResponse> articles = articleService.findAllArticles();
        return ResponseEntity.ok(articles); // 200 OK
    }

    // ==================================================================================
    // 3. READ BY ID (GET /{id})
    // Gibt ArticleResponse zurück
    // ==================================================================================
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticleById(@PathVariable Long id) {
        try {
            ArticleResponse article = articleService.findArticleById(id);
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
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest articleDetails) {
        try {
            ArticleResponse updatedArticle = articleService.updateArticle(id, articleDetails);
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