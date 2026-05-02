package com.example.application.data.goodsreceipts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, Long> {

    List<GoodsReceiptItem> findByGoodsReceiptId(Long goodsReceiptId);

    void deleteByGoodsReceiptId(Long goodsReceiptId);

    /**
     * Setzt alle Items eines Wareneingangs atomar von oldStatus auf newStatus.
     * clearAutomatically=true invalidiert den Hibernate First-Level-Cache nach dem Bulk-UPDATE.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE GoodsReceiptItem i SET i.status = :newStatus WHERE i.goodsReceipt.id = :receiptId AND i.status = :oldStatus")
    int updateStatusByReceiptId(@Param("receiptId") Long receiptId,
                                @Param("newStatus") GoodsReceiptItemStatus newStatus,
                                @Param("oldStatus") GoodsReceiptItemStatus oldStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM goods_receipt_item WHERE article_info_id IN (:articleIds)", nativeQuery = true)
    void deleteByArticleIdIn(@Param("articleIds") List<Long> articleIds);
}
