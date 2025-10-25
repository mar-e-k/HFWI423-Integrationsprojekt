package fhdw.de.einkauf_service.repository;

import fhdw.de.einkauf_service.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    /**
     * Finds an Article by its unique article number (GTIN).
     * Used for duplicate check.
     */
    Optional<Article> findByArticleNumber(String articleNumber);
}