package de.fhdw.vendix.store.core.domain.article;

import de.fhdw.vendix.commons.spring.data.crud.AbstractCrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
class ArticleServiceImpl extends AbstractCrudService<Article, Long> implements ArticleService  {

    private final ArticleRepository articleRepository;

    ArticleServiceImpl(ArticleRepository articleRepository) {
        super(articleRepository);
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Article> findByGtin(String gtin) {
        if (gtin == null || gtin.isEmpty()) {
            return Optional.empty();
        }
        return articleRepository.findByArticleNumber(gtin);
    }
}