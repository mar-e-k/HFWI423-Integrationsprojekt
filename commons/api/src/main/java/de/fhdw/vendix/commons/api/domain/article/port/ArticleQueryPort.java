package de.fhdw.vendix.commons.api.domain.article.port;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.structure.port.QueryPort;

import java.util.Optional;

public interface ArticleQueryPort extends QueryPort {
    Optional<ArticleDTO> findByGtin(String gtin);
}