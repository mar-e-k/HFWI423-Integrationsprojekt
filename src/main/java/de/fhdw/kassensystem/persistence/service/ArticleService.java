package de.fhdw.kassensystem.persistence.service;

import de.fhdw.kassensystem.persistence.entity.Article;
import de.fhdw.kassensystem.persistence.repository.ArticleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService extends CrudService<Article,Long> {
    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        super(articleRepository);
        this.articleRepository = articleRepository;
    }

    public List<Article> findByNameContainingIgnoreCase(String searchTerm) {
        return articleRepository.findByNameContainingIgnoreCase(searchTerm);
    }

    public Optional<Article> findByArticleNumber(String articleNumber) {
        return articleRepository.findByArticleNumber(articleNumber);
    }
}