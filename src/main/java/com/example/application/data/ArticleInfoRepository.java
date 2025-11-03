package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ArticleInfoRepository
        extends JpaRepository<ArticleInfo, Long>, JpaSpecificationExecutor<ArticleInfo> {
    @Modifying
    @Query("""
       update ArticleInfo a
          set a.stockLevel = a.stockLevel + :delta
        where a.id = :id
          and (a.stockLevel + :delta) >= 0
       """)
    int applyStockDelta(@Param("id") Long id, @Param("delta") int delta);
}