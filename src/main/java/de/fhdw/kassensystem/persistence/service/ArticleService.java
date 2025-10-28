package de.fhdw.kassensystem.persistence.service;

import de.fhdw.kassensystem.persistence.entity.Article;
import de.fhdw.kassensystem.persistence.repository.ArticleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArticleService {
    private ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id).stream().filter(article -> article.getArticleNumber().equals(5L)).findFirst();
    }
}