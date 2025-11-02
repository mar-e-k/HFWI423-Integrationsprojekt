package de.fhdw.kassensystem.persistence.repository;

import de.fhdw.kassensystem.persistence.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article,Long> {
    List<Article> findByNameContainingIgnoreCase(String searchTerm);
    Optional<Article> findByArticleNumber(String articleNumber);
}