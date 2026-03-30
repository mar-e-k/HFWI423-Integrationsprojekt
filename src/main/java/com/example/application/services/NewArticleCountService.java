package com.example.application.services;

import org.springframework.stereotype.Service;

@Service
public class NewArticleCountService {

    private final ArticleSyncService articleSyncService;

    public NewArticleCountService(ArticleSyncService articleSyncService) {
        this.articleSyncService = articleSyncService;
    }

    public int getCount() {
        return (int) articleSyncService.countNewArticles();
    }
}