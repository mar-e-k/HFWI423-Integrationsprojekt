package fhdw.de.einkauf_service.repository;

import fhdw.de.einkauf_service.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    /**
     * Finds an Article by its unique article number (GTIN).
     * Used for duplicate check.
     */
    Optional<Article> findByArticleNumber(String articleNumber);
}