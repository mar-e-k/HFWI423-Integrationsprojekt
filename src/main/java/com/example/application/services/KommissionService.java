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


    public String getArticleNameByNumber(String articleNumber) {
        ArticleInfo article = articleRepo.findByArticleNumber(articleNumber);
        return article.getName();
    }

    public String getStorageLocationForArticle(String articleNumber) {
        return articleRepo.findStorageLocationByArticleNumber(articleNumber);
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



    @Transactional
    public void createWeeklyKommissionen() {

        // 1. Alle Stores finden
        List<String> stores = msgRepo.findDistinctStoresWithUnprocessed();

        for (String store : stores) {

            // 2. Neue Kommission erstellen
            Kommission kom = new Kommission();
            kom.setStoreId(store);
            kom.setDate(LocalDateTime.now());
            kom.setFinished(false);
            kom.setOrderPickingNumber(generateNextOrderPickingNumber());

            komRepo.save(kom);

            // 3. (Optional) Messages als verarbeitet markieren
            // Damit sie nicht noch einmal verwendet werden
            List<MessageLogistic> msgs = msgRepo.findByStoreId(store);
            msgs.forEach(m -> {
                // m.setProcessed(true); // falls du ein processed-Feld ergänzt
                msgRepo.save(m);
            });
        }
    }

    @Query("select max(k.orderPickingNumber) from Kommission k")
    public int generateNextOrderPickingNumber() {
        Integer last = komRepo.findMaxOrderNumber();
        return (last == null ? 1 : last + 1);
    }

}
