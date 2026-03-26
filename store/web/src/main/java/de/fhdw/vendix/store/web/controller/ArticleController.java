package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.article.web.ArticleEndpoints;
import de.fhdw.vendix.commons.api.domain.article.web.ArticleQueryApi;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ArticleEndpoints.BASE)
class ArticleController {

    private final ArticleQueryApi articleQueryApi;

    ArticleController(ArticleQueryApi articleQueryApi) {
        this.articleQueryApi = articleQueryApi;
    }

    @GetMapping(ArticleEndpoints.BY_GTIN)
    public ResponseEntity<ArticleDTO> getArticleByGTIN(@PathVariable String gtin) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(articleQueryApi.findByGtin(gtin)
                        .orElseThrow(EntityNotFoundException::new));
    }
}