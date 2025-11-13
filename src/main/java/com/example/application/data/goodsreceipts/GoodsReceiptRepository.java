package com.example.application.data.goodsreceipts;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    Optional<GoodsReceipt> findByReceiptNumber(String receiptNumber);
}
