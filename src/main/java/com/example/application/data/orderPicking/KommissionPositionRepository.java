package com.example.application.data.orderPicking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KommissionPositionRepository extends JpaRepository<KommissionPosition, Long> {
    List<KommissionPosition> findByKommission(Kommission kommission);
    
    //löschen aufgrund der verknüfung zur article info db
    int deleteByArticle_id_ArticleId(Long articleId);
}