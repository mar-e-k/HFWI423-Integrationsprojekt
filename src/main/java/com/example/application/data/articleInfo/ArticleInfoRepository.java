package com.example.application.data.articleInfo;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Query(value = "SELECT * FROM article_info WHERE article_number = :articleNumber LIMIT 1", nativeQuery = true)
    ArticleInfo findByArticleNumber(@Param("articleNumber") String articleNumber);

    @Query("select a.articleNumber from ArticleInfo a")
    Set<String> findAllArticleNumbers();

    @Query("select a.articleId from ArticleInfo a where a.articleId is not null")
    Set<Long> findAllArticleIds();

    @Query(value = "SELECT * FROM article_info WHERE article_id = :articleId LIMIT 1", nativeQuery = true)
    ArticleInfo findByArticleId(@Param("articleId") Long articleId);

    @Query("select a.storageLocation from ArticleInfo a where a.articleNumber = :articleNumber")
    String findStorageLocationByArticleNumber(@Param("articleNumber") String articleNumber);

    @Query("select a.piecesPerPallet from ArticleInfo a where a.articleNumber = :articleNumber")
    int findPiecesPerPalletLevelForArticle(@Param("articleNumber") String articleNumber);

    @Query("select a.reservePallets from ArticleInfo a where a.articleNumber = :articleNumber")
    int findReservedPaletts(@Param("articleNumber") String articleNumber);

    @Query("select a.stockLevel from ArticleInfo a where a.articleNumber = :articleNumber")
    int findStockLevelForArticle(@Param("articleNumber") String articleNumber);
    
    int deleteByArticleId(Long articleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ArticleInfo a where a.articleNumber like 'SIM-%'")
    int deleteAllSimArticles();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from ArticleInfo a where a.id = :id")
    Optional<ArticleInfo> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT a FROM ArticleInfo a WHERE a.articleId IN :articleIds")
    List<ArticleInfo> findAllByArticleIdIn(@Param("articleIds") Collection<Long> articleIds);

    @Query("SELECT a FROM ArticleInfo a WHERE a.articleNumber IN :articleNumbers")
    List<ArticleInfo> findAllByArticleNumberIn(@Param("articleNumbers") Collection<String> articleNumbers);

    @Query("SELECT a FROM ArticleInfo a WHERE a.minStock IS NULL OR a.reservePallets < a.minStock")
    List<ArticleInfo> findAllRequiringRestock();

}