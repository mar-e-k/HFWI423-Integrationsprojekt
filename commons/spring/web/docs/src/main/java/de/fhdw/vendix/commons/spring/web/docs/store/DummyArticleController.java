package de.fhdw.vendix.commons.spring.web.docs.store;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.spring.web.api.store.ArticleApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DummyArticleController implements ArticleApi {

    @Override
    public ResponseEntity<ArticleDTO> getArticleByGtin(String gtin) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ArticleDTO> getArticleById(Long id) {
        return ResponseEntity.noContent().build();
    }
}