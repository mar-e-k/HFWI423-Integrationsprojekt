package de.fhdw.kassensystem.persistence.service;

import de.fhdw.kassensystem.persistence.entity.Article;
import de.fhdw.kassensystem.persistence.repository.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {
    private static final Logger log = LoggerFactory.getLogger(ArticleService.class);
    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    public List<Article> findAll() {
        List<Article> articles = articleRepository.findAll();
        log.info("{} Artikel aus der Datenbank geladen.", articles.size());
        return articles;
    }

    public List<Article> searchByName(String searchTerm) {
        List<Article> articles = articleRepository.findByNameContainingIgnoreCase(searchTerm);
        log.info("{} Artikel für den Suchbegriff '{}' gefunden.", articles.size(), searchTerm);
        return articles;
    }
}