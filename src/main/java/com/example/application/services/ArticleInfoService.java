package com.example.application.services;

import com.example.application.data.ArticleInfo;
import com.example.application.data.ArticleInfoRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;

@Service
public class ArticleInfoService {

    private final ArticleInfoRepository repository;

    public ArticleInfoService(ArticleInfoRepository repository) {
        this.repository = repository;
    }

    public Optional<ArticleInfo> get(Long id) {
        return repository.findById(id);
    }

    public ArticleInfo save(ArticleInfo entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<ArticleInfo> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<ArticleInfo> list(Pageable pageable, Specification<ArticleInfo> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }
    public long count(Specification<ArticleInfo> filter) {
        return repository.count(filter);
    }

}
