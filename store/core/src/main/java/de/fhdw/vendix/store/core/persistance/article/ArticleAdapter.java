package de.fhdw.vendix.store.core.persistance.article;

import de.fhdw.vendix.commons.api.domain.article.dto.ArticleDTO;
import de.fhdw.vendix.commons.api.domain.article.port.ArticleCommandPort;
import de.fhdw.vendix.commons.api.domain.article.port.ArticleQueryPort;
import de.fhdw.vendix.commons.spring.core.crud.AbstractSpringDataCrudLogAdapter;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class ArticleAdapter extends AbstractSpringDataCrudLogAdapter<Article, Long> implements ArticleCommandPort, ArticleQueryPort {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    public ArticleAdapter(ArticleRepository articleRepository, ArticleMapper articleMapper) {
        super(articleRepository);
        this.articleRepository = articleRepository;
        this.articleMapper = articleMapper;
    }

    @Override
    public Optional<ArticleDTO> findByID(long id) {
        return articleRepository.findById(id)
                .map(articleMapper::toDTO);
    }

    @Override
    public Optional<ArticleDTO> findByGTIN(long gtin) {
        return articleRepository.findByArticleNumber(String.valueOf(gtin))
                .map(articleMapper::toDTO);
    }
}