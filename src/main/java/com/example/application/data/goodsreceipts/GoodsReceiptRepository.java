package com.example.application.data.goodsreceipts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    Optional<GoodsReceipt> findByReceiptNumber(String receiptNumber);

    @Query("SELECT DISTINCT g.supplierName FROM GoodsReceipt g WHERE g.supplierName IS NOT NULL ORDER BY g.supplierName")
    List<String> findDistinctSupplierNames();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM wareneingang.goods_receipt WHERE id NOT IN (SELECT DISTINCT goods_receipt_id FROM wareneingang.goods_receipt_item)", nativeQuery = true)
    int deleteReceiptsWithNoItems();
}
