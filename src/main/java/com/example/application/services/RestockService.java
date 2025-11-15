package com.example.application.services;

import com.example.application.data.article.ArticleInfo;
import com.example.application.data.article.ArticleInfoRepository;
import com.example.application.data.article.RestockItem;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

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
    public List<RestockItem> getArticlesToRestock() {

        return articleInfoRepository.findAll().stream()

                .filter(a -> {
                    Integer min = a.getMinStock();
                    if (min == null) {
                        // Wenn kein Mindestbestand gesetzt ist → trotzdem anzeigen
                        return true;
                    }
                    return a.getStockLevel() < min;
                })

                .map(RestockItem::new)
                .collect(Collectors.toList());
    }
}