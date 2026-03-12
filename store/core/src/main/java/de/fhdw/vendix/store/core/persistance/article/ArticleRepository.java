package de.fhdw.vendix.store.core.persistance.article;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ArticleRepository extends JpaRepository<Article,Long> {
    Optional<Article> findByArticleNumber(String articleNumber);
}