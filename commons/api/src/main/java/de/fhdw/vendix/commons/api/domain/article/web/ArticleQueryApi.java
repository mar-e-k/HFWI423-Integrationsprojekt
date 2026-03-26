package de.fhdw.vendix.commons.api.domain.article.web;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.structure.web.QueryApi;

import java.util.Optional;

public interface ArticleQueryApi extends QueryApi {
    Optional<ArticleDTO> findByGtin(String gtin);
}