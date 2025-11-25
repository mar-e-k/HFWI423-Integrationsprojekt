package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import de.fhdw.fillialensystem.persistence.repository.ArticleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArticleService extends AbstractCrudService<Article,Long> {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        super(articleRepository);
        this.articleRepository = articleRepository;
    }

    public Optional<Article> findByArticleNumber(String articleNumber) {
        return articleRepository.findByArticleNumber(articleNumber);
    }

    @Override
    @Deprecated
    public Article create(Article entity) {
        throw new UnsupportedOperationException("Operation 'Create' is not supported for articles");
    }

    @Override
    @Deprecated
    public Article update(Long aLong, Article updateToEntity) {
        throw new UnsupportedOperationException("Operation 'Update' is not supported for articles");
    }

    @Override
    @Deprecated
    public void delete(Long aLong) {
        throw new UnsupportedOperationException("Operation 'Delete' is not supported for articles");
    }
}