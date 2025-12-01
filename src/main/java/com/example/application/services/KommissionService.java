package com.example.application.services;

import com.example.application.data.article.ArticleInfo;
import com.example.application.data.article.ArticleInfoRepository;
import com.example.application.data.orderPicking.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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


    public String getArticleName(String articleId) {
        ArticleInfo artikel = artikelService.findById(Long.valueOf(articleId)); // hier auf ArticleInfoService zugreifen
        return artikel != null ? artikel.getName() : "Unbekannt";
    }

    /**
     * Bestätigt eine einzelne Position, ggf. mit abweichender Menge.
     */
    @Transactional
    public void bestätigePosition(Long positionId, int gelieferteMenge, String grund, String ersteller) {
        KommissionPosition pos = posRepo.findById(positionId)
                .orElseThrow(() -> new EntityNotFoundException("Position nicht gefunden"));

        ArticleInfo artikel = pos.getArticle_id();

        int geplant = pos.getAmount();
        pos.setAmount(gelieferteMenge);

        // Bestandsreduktion um gelieferte Menge
        artikelService.reduceStock(artikel, gelieferteMenge);

        posRepo.save(pos);
    }

    public List<KommissionPosition> getPositionenFürKommission(Kommission kommission) {
        return posRepo.findByKommission(kommission);
    }


    /**
     * Prüft, ob eine Kommission abgeschlossen werden kann (alle Positionen bearbeitet).
     * Wenn ja, wird sie abgeschlossen und ausgegraut dargestellt.
     */
    //@Transactional

    /**
     * public void schließeKommission(Long kommissionId) {
     * Kommission k = komRepo.findById(kommissionId)
     * .orElseThrow(() -> new EntityNotFoundException("Kommission nicht gefunden"));
     * <p>
     * boolean alleBearbeitet = k.getPositionen()
     * .stream()
     * .allMatch(KommissionPosition::isBearbeitet);
     * <p>
     * if (!alleBearbeitet) {
     * throw new IllegalStateException("Kommission kann nicht abgeschlossen werden: noch offene Positionen.");
     * }
     * <p>
     * k.setFinished(true);
     * komRepo.save(k);
     * }
     */

    @Transactional
    public Kommission erstelleKommissionFürStore(String storeId) {

        // 1. Alle fehlenden Artikel dieser Filiale holen
        List<MessageLogistic> fehlendeArtikel =
                msgRepo.findByStoreIdAndQuantityGreaterThan(storeId, 0);

        if (fehlendeArtikel.isEmpty()) {
            throw new IllegalStateException("Keine fehlenden Artikel für Store " + storeId);
        }

        // 2. Kommission anlegen
        Kommission kom = new Kommission();
        kom.setFinished(false);
        kom.setDate(LocalDateTime.now());
        kom.setStoreId(storeId);

        // Nummer setzen
        kom.setOrderPickingNumber(komRepo.nextOrderNumber());
        kom = komRepo.save(kom);

        // 3. Positionen anlegen
        for (MessageLogistic m : fehlendeArtikel) {

            ArticleInfo artikelInfo = articleRepo.findByArticleNumber((m.getArticleNumber()));


            KommissionPosition pos = new KommissionPosition();
            pos.setKommission(kom);
            pos.setArticle_id(artikelInfo);
            pos.setAmount((int) m.getQuantity());
            pos.setStorageLocation(artikelInfo.getStorageLocation());

            posRepo.save(pos);
        }

        return kom;
    }

}