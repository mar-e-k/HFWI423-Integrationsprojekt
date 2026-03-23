package de.fhdw.vendix.store.core.persistance.article;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.article.port.ArticleCommandPort;
import de.fhdw.vendix.commons.api.domain.article.port.ArticleQueryPort;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class ArticleAdapter extends AbstractDtoCrudAdapter<Article, ArticleDTO, Long> implements ArticleQueryPort, ArticleCommandPort {

    private final ArticleEntityAdapter articleEntityAdapter;
    private final ArticleMapper articleMapper;

    ArticleAdapter(ArticleEntityAdapter articleEntityAdapter, ArticleMapper articleMapper) {
        super(articleEntityAdapter, articleMapper);
        this.articleEntityAdapter = articleEntityAdapter;
        this.articleMapper = articleMapper;
    }
}