package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.model.Article;
import fhdw.de.einkauf_service.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController // RESTful API Controller
@RequestMapping("/api/v1/articles") // Base URL for all endpoints in this controller
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * POST /api/v1/articles
     * Endpoint to create a new article.
     * @param article The article data received in the request body.
     * @return ResponseEntity with the created article and HTTP status 201.
     */
    @PostMapping
    public ResponseEntity<Article> createArticle(@Valid @RequestBody Article article) {
        try {
            // @Valid annotation triggers bean validation (checks @NotBlank, @NotNull etc.)
            Article createdArticle = articleService.createNewArticle(article);

            // Returns 201 Created and the new article object
            return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            // Handles the duplicate GTIN check from the service layer
            return ResponseEntity.status(HttpStatus.CONFLICT) // 409 Conflict
                    .body(null);
        } catch (Exception e) {
            // Generic error handling
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Server Error
                    .body(null);
        }
    }

    /**
     * GET /api/v1/articles/{id}
     * Retrieves an article by its unique ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        try {
            Article article = articleService.findArticleById(id);
            return ResponseEntity.ok(article); // Returns 200 OK
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    /**
     * GET /api/v1/articles
     * @return all articles
     */
    @GetMapping
    public ResponseEntity<List<Article>> getAllArticles() {
        List<Article> articles = articleService.findAllArticles();
        return ResponseEntity.ok(articles); // 200 OK
    }

    /**
     * PUT /api/v1/articles/{id}
     * Endpoint to update an existing article
     */
    @PutMapping("/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable Long id, @Valid @RequestBody Article articleDetails) {
        try {
            Article updatedArticle = articleService.updateArticle(id, articleDetails);
            return ResponseEntity.ok(updatedArticle); // 200 OK
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }

    /**
     * DELETE /api/v1/articles/{id}
     * Endpoint to delete an existing article
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        try {
            articleService.deleteArticle(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content (Standard für erfolgreiches Löschen)
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404 Not Found
        }
    }
}