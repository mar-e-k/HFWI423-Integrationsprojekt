package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.spring.web.server.store.api.ArticleApi;
import de.fhdw.vendix.store.core.domain.article.ArticleMapper;
import de.fhdw.vendix.store.core.domain.article.ArticleService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
class ArticleController implements ArticleApi {

    private final ArticleService articleService;
    private final ArticleMapper articleMapper;

    ArticleController(ArticleService articleService, ArticleMapper articleMapper) {
        this.articleService = articleService;
        this.articleMapper = articleMapper;
    }

    @Override
    public ResponseEntity<Set<ArticleDTO>> getArticles(
            @Nullable Long id
    ) {
        Set<ArticleDTO> filtered = articleService.findAll().stream()
                .filter(a -> id == null || Objects.equals(a.getId(), id))
                .map(articleMapper::toDTO)
                .collect(Collectors.toUnmodifiableSet());
        return ResponseEntity.ok(filtered);
    }

    @Override
    public ResponseEntity<ArticleDTO> getArticleById(Long id) {
        Optional<ArticleDTO> articleDTO = articleService.findById(id).map(articleMapper::toDTO);
        return ResponseEntity.of(articleDTO);
    }

    @Override
    public ResponseEntity<ArticleDTO> getArticleByGtin(Long gtin) {
        Optional<ArticleDTO> articleDTO = articleService.findByGtin(String.valueOf(gtin)).map(articleMapper::toDTO);
        return ResponseEntity.of(articleDTO);
    }
}