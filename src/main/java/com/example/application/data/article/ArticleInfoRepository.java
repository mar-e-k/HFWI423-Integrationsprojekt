package com.example.application.data.article;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
    @Query("select distinct a.storageLocation from ArticleInfo a order by a.storageLocation")
    List<String> findDistinctStorageLocations();

    @Modifying
    @Query("update ArticleInfo a set a.storageLocation = :newLocation " +
            "where a.storageLocation = :oldLocation")
    int bulkUpdateStorageLocation(@Param("oldLocation") String oldLocation,
                                  @Param("newLocation") String newLocation);

    boolean existsByStorageLocation(String storageLocation);
}