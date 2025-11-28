package com.example.application.data.restockorder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RestockOrderRepository extends JpaRepository<RestockOrder, Long> {

    boolean existsByArticle_IdAndDeliveredFalse(Long articleId);
}
