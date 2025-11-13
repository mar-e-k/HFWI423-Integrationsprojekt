package com.example.application.services;

import com.example.application.data.article.ArticleInfo;
import com.example.application.data.article.ArticleInfoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestockService {

    private final ArticleInfoRepository articleInfoRepository;

    public RestockService(ArticleInfoRepository articleInfoRepository) {
        this.articleInfoRepository = articleInfoRepository;
    }

    /**
     * Gibt alle Artikel zurück, deren Lagerbestand unter dem Mindestbestand liegt.
     */
    public List<ArticleInfo> getArticlesToRestock() {
        return articleInfoRepository.findAll().stream()
                .filter(a -> a.getStockLevel() < a.getMinStock())
                .collect(Collectors.toList());
    }
}