package com.example.application.data.orderPicking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.List;

@Repository
public interface KommissionPositionRepository extends JpaRepository<KommissionPosition, Long> {
    List<KommissionPosition> findByKommission(Kommission kommission);
    
    //löschen aufgrund der verknüfung zur article info db
    @Transactional
    @Modifying
    @Query("delete from KommissionPosition k where k.article_id.articleId = :articleId")
    int deleteByArticleId(@Param("articleId") Long articleId);
}
