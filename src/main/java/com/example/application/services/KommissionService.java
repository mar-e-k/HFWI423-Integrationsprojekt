package com.example.application.services;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class KommissionService {

    @Autowired
    private KommissionRepository komRepo;
    @Autowired
    private KommissionPositionRepository posRepo;
    @Autowired
    private ArticleInfoService artikelService;
    @Autowired
    private MessageLogisticRepository msgRepo;
    @Autowired
    private ArticleInfoRepository articleRepo;


    public String getArticleNameByNumber(String number) {
        ArticleInfo article = articleRepo.findByArticleNumber(number);

        if (article == null) {
            return "Unbekannter Artikel";
        }

        return article.getName();
    }
    public String getStorageLocationForArticle(String articleNumber) {
        return articleRepo.findStorageLocationByArticleNumber(articleNumber);
    }

    public int getStockLevelForArticle(String articleNumber) {
        return articleRepo.findStockLevelForArticle(articleNumber);
    }


    public List<Kommission> getOffeneKommissionen() {
        return komRepo.findByFinishedFalseOrderByDateAsc();
    }

    public List<Kommission> getAlleKommissionen() {
        return komRepo.findAllByOrderByDateAsc();
    }

    public Kommission save(Kommission k) {
        return komRepo.save(k);
    }

    public boolean articleExists(String articleNumber) {
        return articleRepo.findByArticleNumber(articleNumber) != null;
    }


    public int generateNextOrderPickingNumber() {
        Integer last = komRepo.findMaxOrderNumber();
        return (last == null ? 1 : last + 1);
    }

}
