package com.example.application.services;

import com.example.application.data.article.ArticleInfo;
import com.example.application.data.orderPicking.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    /**
     * Liefert alle Kommissionen, die noch nicht abgeschlossen sind.
     */
    public List<Kommission> getOffeneKommissionen() {
        return komRepo.findByFinishedFalseOrderByDateAsc();
    }

    public Kommission save(Kommission k) {
        return komRepo.save(k);
    }


    public String getArticleName(ArticleInfo articleId) {
        ArticleInfo artikel = artikelService.findById(articleId.getId()); // hier auf ArticleInfoService zugreifen
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

        // 1. Artikel finden, die unter dem Sollbestand liegen
        List<MessageLogistic> artikel = msgRepo
                .findUnderstocked(storeId);

        if (artikel.isEmpty()) {
            throw new RuntimeException("Keine Artikel unter Sollbestand für Store " + storeId);
        }

        // 2. Neue Kommission anlegen
        Kommission kom = new Kommission();
        kom.setFinished(false);
        kom.setDate(LocalDateTime.now());
        kom.setStore(storeId);
        kom = komRepo.save(kom); // Speichern, damit ID existiert

        // 3. Zu jeder Zeile eine Position anlegen
        for (MessageLogistic m : artikel) {

            ArticleInfo artikelInfo = artikelService.findById((long) m.getArticleNumber());

            KommissionPosition pos = new KommissionPosition();
            pos.setKommission(kom);
            pos.setArticle_id(artikelInfo);

            int menge = m.getTargetStockLevel() - m.getStockLevel();
            pos.setAmount(menge);

            // Lagerplatz aus ArticleInfo
            pos.setLagerplatz(artikelInfo.getStorageLocation());

            posRepo.save(pos);
        }

        return kom;
    }
}