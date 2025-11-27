package com.example.application.data.contingent;

import com.example.application.data.article.ArticleInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContingentRepository extends JpaRepository<Contingent, Long> {

    Optional<Contingent> findByArticle(ArticleInfo article);
}
