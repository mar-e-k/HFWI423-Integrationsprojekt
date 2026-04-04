package de.fhdw.vendix.store.core.domain.article;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.spring.data.crud.AbstractDtoCrudAdapter;
import de.fhdw.vendix.store.core.domain.article.service.ArticleService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class ArticleAdapter extends AbstractDtoCrudAdapter<Article, ArticleDTO, Long> implements ArticleService {

    private final ArticleEntityAdapter articleEntityAdapter;
    private final ArticleMapper articleMapper;

    ArticleAdapter(ArticleEntityAdapter articleEntityAdapter, ArticleMapper articleMapper) {
        super(articleEntityAdapter, articleMapper);
        this.articleEntityAdapter = articleEntityAdapter;
        this.articleMapper = articleMapper;
    }

    @Override
    public Optional<ArticleDTO> findByGtin(String gtin) {
        return articleEntityAdapter.findByGtin(gtin)
                .map(articleMapper::toDTO);
    }
}