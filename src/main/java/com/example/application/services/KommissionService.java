package com.example.application.services;

import com.example.application.data.article.ArticleInfo;
import com.example.application.data.article.ArticleInfoRepository;
import com.example.application.data.orderPicking.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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

    public String getStockLevelForArticle(String articleNumber) {
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


    @Query("select max(k.orderPickingNumber) from Kommission k")
    public int generateNextOrderPickingNumber() {
        Integer last = komRepo.findMaxOrderNumber();
        return (last == null ? 1 : last + 1);
    }

}
