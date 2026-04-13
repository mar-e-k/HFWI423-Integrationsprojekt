package de.fhdw.vendix.store.core.domain.article;

import de.fhdw.vendix.commons.spring.data.service.CrudService;

import java.util.Optional;

public interface ArticleService extends CrudService<Article, Long> {
    Optional<Article> findByGtin(String gtin);
}