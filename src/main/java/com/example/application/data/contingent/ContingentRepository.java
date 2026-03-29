package com.example.application.data.contingent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContingentRepository extends JpaRepository<Contingent, Long> {

    List<Contingent> findAllByArticleId(Long articleId);

    boolean existsByArticleId(Long articleId);

    @Query("SELECT COALESCE(SUM(c.availableQuantity), 0) FROM Contingent c WHERE c.articleId = :articleId")
    Integer sumAvailableQuantityByArticleId(@Param("articleId") Long articleId);
}