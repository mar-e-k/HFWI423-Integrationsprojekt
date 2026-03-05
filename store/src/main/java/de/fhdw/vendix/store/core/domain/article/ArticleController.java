package de.fhdw.vendix.store.core.domain.article;

import de.fhdw.vendix.commons.api.domain.article.ArticleEndpoints;
import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.article.port.ArticleQueryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ArticleEndpoints.BASE)
@Tag(name = "Article", description = "Endpoints for operations related to articles")
class ArticleController {

    private final ArticleQueryPort articleQueryPort;

    public ArticleController(ArticleQueryPort articleQueryPort) {
        this.articleQueryPort = articleQueryPort;
    }

    @GetMapping(ArticleEndpoints.BY_ID)
    @Operation(summary = "Get article by id")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(articleQueryPort.findByID(id)
                        .orElseThrow(EntityNotFoundException::new));
    }

    @GetMapping(ArticleEndpoints.BY_GTIN)
    @Operation(summary = "Get article by GTIN")
    public ResponseEntity<ArticleDTO> getArticleByGTIN(@PathVariable long gtin) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(articleQueryPort.findByGTIN(gtin)
                        .orElseThrow(EntityNotFoundException::new));
    }
}