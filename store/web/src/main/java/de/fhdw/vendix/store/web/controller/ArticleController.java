package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.spring.web.server.store.api.ArticleApi;
import de.fhdw.vendix.commons.spring.web.server.store.model.ArticleDTO;
import de.fhdw.vendix.store.core.domain.article.ArticleMapper;
import de.fhdw.vendix.store.core.domain.article.ArticleService;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class ArticleController implements ArticleApi {

    private final ArticleService articleService;
    private final ArticleMapper articleMapper;

    ArticleController(ArticleService articleService, ArticleMapper articleMapper) {
        this.articleService = articleService;
        this.articleMapper = articleMapper;
    }

    @Override
    public ResponseEntity<List<ArticleDTO>> getArticles(@Nullable Long id) {
        return ArticleApi.super.getArticles(id);
    }
}