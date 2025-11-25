package de.fhdw.fillialensystem.api.controller;

import de.fhdw.commons.api.dto.ArticleDTO;
import de.fhdw.fillialensystem.api.mapper.ArticleMapper;
import de.fhdw.fillialensystem.persistence.service.ArticleService;
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
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleMapper articleMapper;

    public ArticleController(ArticleService articleService, ArticleMapper articleMapper) {
        this.articleService = articleService;
        this.articleMapper = articleMapper;
    }

    @GetMapping
    public ResponseEntity<List<ArticleDTO>> findAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(articleService.findAll().stream()
                        .map(articleMapper::toDto)
                        .toList()
                );
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ArticleDTO> findById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(articleMapper.toDto(articleService.findById(id)
                        .orElseThrow(EntityNotFoundException::new))
                );
    }

    @GetMapping("/number/{articleNumber}")
    public ResponseEntity<ArticleDTO> findByArticleNumber(@PathVariable String articleNumber) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(articleMapper.toDto(articleService.findByArticleNumber(articleNumber)
                        .orElseThrow(EntityNotFoundException::new))
                );
    }
}