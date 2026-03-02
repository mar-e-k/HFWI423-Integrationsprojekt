package de.fhdw.vendix.store.core.domain.article;

import de.fhdw.vendix.store.core.domain.AbstractCrudService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArticleService extends AbstractCrudService<Article,Long> {

    public ArticleService(ArticleRepository articleRepository) {
        super(articleRepository);
    }

    public Optional<Article> findByArticleNumber(String articleNumber) {
        return ((ArticleRepository) repository).findByArticleNumber(articleNumber);
    }

    @Override
    @Deprecated
    public Article create(Article entity) {
        throw new UnsupportedOperationException("Operation 'Create' is not supported for articles");
    }

    @Override
    @Deprecated
    public Article update(Article entity) {
        throw new UnsupportedOperationException("Operation 'Update' is not supported for articles");
    }

    @Override
    @Deprecated
    public void delete(Long id) {
        throw new UnsupportedOperationException("Operation 'Delete' is not supported for articles");
    }
}