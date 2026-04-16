package com.example.application.data.contingent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ContingentRepository extends JpaRepository<Contingent, Long> {

    List<Contingent> findAllByArticleId(Long articleId);

    boolean existsByArticleId(Long articleId);

    @Query("SELECT COALESCE(SUM(c.availableQuantity), 0) FROM Contingent c WHERE c.articleId = :articleId")
    Integer sumAvailableQuantityByArticleId(@Param("articleId") Long articleId);

    @Query("""
            SELECT COUNT(DISTINCT c.articleId)
            FROM Contingent c, ExternalArticle ea
            WHERE ea.id = c.articleId
              AND NOT EXISTS (
                SELECT 1 FROM ArticleInfo ai WHERE ai.articleNumber = ea.articleNumber
              )
            """)
    long countNewArticles();

    @Modifying
    @Query("delete from Contingent c where c.articleId in :ids")
    int deleteByArticleIdIn(@Param("ids") Collection<Long> ids);
}