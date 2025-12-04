package com.example.application.services;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import com.example.application.data.contingent.Contingent;
import com.example.application.data.contingent.ContingentRepository;
import com.example.application.data.externalArticle.ExternalArticle;
import com.example.application.data.externalArticle.ExternalArticleRepository;
import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;

@Service
public class ArticleSyncService {

    private final ContingentRepository contingentRepository;
    private final ExternalArticleRepository externalArticleRepository;
    private final ArticleInfoRepository articleInfoRepository;

    public ArticleSyncService(ContingentRepository contingentRepository,
                              ExternalArticleRepository externalArticleRepository,
                              ArticleInfoRepository articleInfoRepository) {
        this.contingentRepository = contingentRepository;
        this.externalArticleRepository = externalArticleRepository;
        this.articleInfoRepository = articleInfoRepository;
    }

    @Transactional
    public void createMissingArticleInfosFromContingents() {
        List<Contingent> contingents = contingentRepository.findAll();

        for (Contingent c : contingents) {
            Long articleId = c.getArticleId();

            ExternalArticle ext = externalArticleRepository.findById(articleId)
                    .orElse(null);
            if (ext == null) {
                continue;
            }

            String articleNumber = ext.getArticleNumber();
            ArticleInfo existing = articleInfoRepository.findByArticleNumber(articleNumber);
            if (existing != null) {
                continue;
            }

            ArticleInfo info = new ArticleInfo();
            info.setArticleNumber(articleNumber);
            info.setName(ext.getName());
            info.setStockLevel(0);
            info.setStorageLocation("UNGESETZT");
            info.setReserveStorageLocation(null);
            info.setMinStock(null);
            info.setPiecesPerPallet(null);
            info.setReservePallets(0);

            articleInfoRepository.save(info);
        }
    }
}
