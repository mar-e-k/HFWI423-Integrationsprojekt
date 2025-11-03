package com.example.application.services;

import com.example.application.data.StockChangeLog;
import com.example.application.data.StockChangeLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import java.util.List;

@Service
public class StockChangeLogService {

    private final StockChangeLogRepository repository;

    public StockChangeLogService(StockChangeLogRepository repository) {
        this.repository = repository;
    }

    public Page<StockChangeLog> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public long count() {
        return repository.count();
    }
    public List<StockChangeLog> findAll(Sort sort) {
        return repository.findAll(sort);
    }
}
