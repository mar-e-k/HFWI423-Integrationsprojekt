package de.fhdw.fillialensystem.api.controller;

import de.fhdw.commons.api.dto.ArticleDTO;
import de.fhdw.fillialensystem.api.mapper.ArticleMapper;
import de.fhdw.fillialensystem.persistence.service.imported.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/article")
@Tag(name = "Article", description = "Endpoints for operations related to articles")
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleMapper articleMapper;

    public ArticleController(ArticleService articleService, ArticleMapper articleMapper) {
        this.articleService = articleService;
        this.articleMapper = articleMapper;
    }

    @GetMapping
    @Operation(summary = "Retrieve all articles")
    public ResponseEntity<List<ArticleDTO>> getArticles() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(articleService.findAll()
                        .stream()
                        .map(articleMapper::toDto)
                        .toList());
    }

    @GetMapping("/id/{id}")
    @Operation(summary = "Retrieve article by id")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(articleService.findById(id)
                        .map(articleMapper::toDto)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping("/gtin/{gtin}")
    @Operation(summary = "Retrieve account by article number")
    public ResponseEntity<ArticleDTO> getArticleByArticleNumber(@PathVariable String gtin) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(articleService.findByArticleNumber(gtin)
                        .map(articleMapper::toDto)
                        .orElseThrow(EntityNotFoundException::new));
    }
}