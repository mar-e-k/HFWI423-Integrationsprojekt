package de.fhdw.kassensystem.persistence.repository;

import de.fhdw.kassensystem.persistence.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article,Long> {
}