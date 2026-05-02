package com.example.application.services;

import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.articleInfo.RestockItem;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import java.util.List;

@Service
public class RestockService {

    private final ArticleInfoRepository articleInfoRepository;

    public RestockService(ArticleInfoRepository articleInfoRepository) {
        this.articleInfoRepository = articleInfoRepository;
    }

    /**
     * Gibt alle Artikel zurück, deren Lagerbestand unter dem Mindestbestand liegt.
     */
    public List<RestockItem> getArticlesToRestock() {
        return articleInfoRepository.findAllRequiringRestock().stream()
                .map(RestockItem::new)
                .collect(Collectors.toList());
    }
}