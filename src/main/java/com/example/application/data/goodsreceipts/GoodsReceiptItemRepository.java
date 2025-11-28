package com.example.application.data.goodsreceipts;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, Long> {

    List<GoodsReceiptItem> findByGoodsReceiptId(Long goodsReceiptId);
}
