package de.fhdw.vendix.store.core.persistance.article.port;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudQueryService;

import java.util.Optional;

interface ArticleQueryService extends CrudQueryService<ArticleDTO, Long> {
    Optional<ArticleDTO> findByGtin(String gtin);
}