package com.example.application.data.stockChanges;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockChangeLogRepository extends JpaRepository<StockChangeLog, Long> {}
