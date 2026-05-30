package de.fhdw.vendix.commons.spring.web.api.store;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.spring.web.api.scheme.KeycloakOpenApiScheme;
import de.fhdw.vendix.commons.spring.web.api.scheme.StoreRoutingOpenApiScheme;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/api/article")
@Tag(
        name = "Article",
        description = "Operations related to articles."
)
@KeycloakOpenApiScheme
@StoreRoutingOpenApiScheme
public interface ArticleApi {

    @Operation(
            summary = "[DNT] Get a list of articles",
            description = "[DNT] TODO"
    )
    @GetExchange
    ResponseEntity<List<ArticleDTO>> getArticles();

    @Operation(
            summary = "Get an article by GTIN",
            description = "Retrieves a single article by its GTIN (Global Trade Item Number)."
    )
    @GetExchange("/gtin/{gtin}")
    ResponseEntity<ArticleDTO> getArticleByGtin(@PathVariable String gtin);

    @Operation(
            summary = "Get an article by ID",
            description = "Retrieves a single article by its unique identifier."
    )
    @GetExchange("/id/{id}")
    ResponseEntity<ArticleDTO> getArticleById(@PathVariable Long id);
}