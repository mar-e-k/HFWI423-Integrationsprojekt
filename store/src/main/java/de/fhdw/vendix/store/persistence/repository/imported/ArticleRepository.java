package de.fhdw.vendix.store.persistence.repository.imported;

import de.fhdw.vendix.store.persistence.entity.imported.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article,Long> {
    Optional<Article> findByArticleNumber(String articleNumber);
}