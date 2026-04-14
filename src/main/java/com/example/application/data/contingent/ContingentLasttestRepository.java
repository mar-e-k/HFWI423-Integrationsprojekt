package com.example.application.data.contingent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ContingentLasttestRepository extends JpaRepository<ContingentLasttest, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM ContingentLasttest c")
    void deleteAllBulk();

    @Query("SELECT COUNT(DISTINCT c.simArticleNumber) FROM ContingentLasttest c WHERE c.simArticleNumber IS NOT NULL")
    long countDistinctSyntheticNewArticles();
}
