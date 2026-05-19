package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import fhdw.de.einkauf_service.dto.ArticleRequestDTO;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;
import fhdw.de.einkauf_service.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/articles")
@Tag(name = "Articles", description = "Verwaltung der Artikel im Sortiment")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    @Operation(summary = "Neuen Artikel anlegen")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Artikel angelegt"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabe (ProblemDetail)"),
            @ApiResponse(responseCode = "409", description = "Artikelnummer/GTIN existiert bereits")
    })
    public ResponseEntity<ArticleResponseDTO> createArticle(@Valid @RequestBody ArticleRequestDTO articleRequestDto) {
        try {
            ArticleResponseDTO createdArticle = articleService.createNewArticle(articleRequestDto);
            return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping
    @Operation(summary = "Artikel suchen / filtern",
            description = "Liefert Artikel anhand der übergebenen Filterkriterien (Query-Parameter aus ArticleFilterDTO).")
    @ApiResponse(responseCode = "200", description = "Trefferliste (ggf. leer)")
    public ResponseEntity<List<ArticleResponseDTO>> searchArticles(ArticleFilterDTO filter) {
        List<ArticleResponseDTO> responseDTOs = articleService.findFilteredArticles(filter);
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Artikel per ID abrufen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artikel gefunden"),
            @ApiResponse(responseCode = "404", description = "Artikel nicht gefunden")
    })
    public ResponseEntity<ArticleResponseDTO> getArticleById(@PathVariable Long id) {
        try {
            ArticleResponseDTO article = articleService.findArticleById(id);
            return ResponseEntity.ok(article);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Artikel aktualisieren")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artikel aktualisiert"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabe (ProblemDetail)"),
            @ApiResponse(responseCode = "404", description = "Artikel nicht gefunden")
    })
    public ResponseEntity<ArticleResponseDTO> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequestDTO articleDetails) {
        try {
            ArticleResponseDTO updatedArticle = articleService.updateArticle(id, articleDetails);
            return ResponseEntity.ok(updatedArticle);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Artikel löschen")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Artikel gelöscht"),
            @ApiResponse(responseCode = "404", description = "Artikel nicht gefunden")
    })
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        try {
            articleService.deleteArticle(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
