package com.example.application.data.goodsreceipts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, Long> {

    List<GoodsReceiptItem> findByGoodsReceiptId(Long goodsReceiptId);

    void deleteByGoodsReceiptId(Long goodsReceiptId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM goods_receipt_item WHERE article_info_id IN (:articleIds)", nativeQuery = true)
    void deleteByArticleIdIn(@Param("articleIds") List<Long> articleIds);
}
