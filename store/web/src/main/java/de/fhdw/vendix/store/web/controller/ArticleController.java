package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.spring.web.server.store.api.ArticleApi;
import de.fhdw.vendix.store.core.domain.article.ArticleMapper;
import de.fhdw.vendix.store.core.domain.article.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
class ArticleController implements ArticleApi {

    private final ArticleService articleService;
    private final ArticleMapper articleMapper;

    ArticleController(ArticleService articleService, ArticleMapper articleMapper) {
        this.articleService = articleService;
        this.articleMapper = articleMapper;
    }

    @Override
    public ResponseEntity<ArticleDTO> getArticleById(Long id) {
        Optional<ArticleDTO> articleDTO = articleService.findById(id).map(articleMapper::toDTO);
        return ResponseEntity.of(articleDTO);
    }

    @Override
    public ResponseEntity<ArticleDTO> getArticleByGtin(String gtin) {
        Optional<ArticleDTO> articleDTO = articleService.findByGtin(gtin).map(articleMapper::toDTO);
        return ResponseEntity.of(articleDTO);
    }
}