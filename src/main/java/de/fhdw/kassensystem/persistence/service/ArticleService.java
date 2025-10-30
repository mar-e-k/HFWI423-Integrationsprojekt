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
        return articleRepository.findAll();
    }

    public List<Article> searchByName(String searchTerm) {
        return articleRepository.findByNameContainingIgnoreCase(searchTerm);
    }
}