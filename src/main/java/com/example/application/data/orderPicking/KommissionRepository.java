package com.example.application.data.orderPicking;

import com.example.application.data.article.ArticleInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KommissionRepository extends JpaRepository<Kommission, Long> {
    List<Kommission> findByFinishedFalseOrderByDateAsc();
    List<Kommission> findAllByOrderByDateAsc();

    @Query("SELECT COALESCE(MAX(k.orderPickingNumber), 0) + 1 FROM Kommission k")
    int nextOrderNumber();

}


