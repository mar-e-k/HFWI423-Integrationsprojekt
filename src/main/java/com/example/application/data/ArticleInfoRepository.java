package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleInfoRepository
        extends JpaRepository<ArticleInfo, Long>, JpaSpecificationExecutor<ArticleInfo> {
}