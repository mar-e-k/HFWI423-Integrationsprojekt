package com.example.application.services;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeeklyKommissionScheduler {

    private final KommissionService kommissionService;
    private final MessageLogisticRepository msgRepo;
    private final ArticleInfoRepository articleRepo;

    public WeeklyKommissionScheduler(KommissionService kommissionService,
                                     MessageLogisticRepository msgRepo,
                                     ArticleInfoRepository articleRepo) {
        this.kommissionService = kommissionService;
        this.msgRepo = msgRepo;
        this.articleRepo = articleRepo;
    }

    @Transactional
    @Scheduled(cron = "0 0 10 * * MON")
    public void createWeeklyKommissionen() {

        // Alle Filialen holen, die noch unverarbeitete Bestellungen haben
        List<String> stores = msgRepo.findDistinctStoresWithUnprocessed();

        for (String storeId : stores) {

            // Alle offenen Bestellnachrichten dieser Filiale laden
            List<MessageLogistic> offeneMessages =
                    msgRepo.findByStoreIdAndQuantityGreaterThanAndProcessedFalse(storeId, 0);

            // Wenn es nichts Offenes gibt, zur nächsten Filiale gehen
            if (offeneMessages.isEmpty()) {
                continue;
            }

            // Hier sammeln wir nur die Artikel, die wirklich lieferbar sind
            List<MessageLogistic> lieferbareMessages = new ArrayList<>();

            for (MessageLogistic msg : offeneMessages) {

                // Den passenden Artikel in unserer Artikel-Tabelle suchen
            	ArticleInfo article = resolveArticle(msg);
            	
                // Wenn der Artikel nicht existiert, wird er übersprungen
                if (article == null) {
                    continue;
                }

                // Wenn insgesamt kein Bestand da ist, wird der Artikel übersprungen
                if (article.getTotalStock() == null || article.getTotalStock() <= 0) {
                    continue;
                }

                // Nur lieferbare Artikel kommen in die spätere Kommission
                lieferbareMessages.add(msg);
            }

            // Wenn kein lieferbarer Artikel übrig ist, wird keine Kommission erzeugt
            if (lieferbareMessages.isEmpty()) {
                continue;
            }

            // Neue Kommission für diese Filiale anlegen
            Kommission k = new Kommission();
            k.setStoreId(storeId);
            k.setDate(LocalDateTime.now());
            k.setFinished(false);
            k.setOrderPickingNumber(
                    kommissionService.generateNextOrderPickingNumber()
            );

            // Kommission speichern
            kommissionService.save(k);

            for (MessageLogistic msg : lieferbareMessages) {
                // Jede lieferbare Nachricht dieser Kommission zuordnen
                msg.setKommission(k);

                // Nachricht als verarbeitet markieren
                msg.setProcessed(true);

                // Änderung in der Datenbank speichern
                msgRepo.save(msg);
            }
        }

        System.out.println("Wöchentliche Kommissionen erstellt.");
    }
    
    //artikel id oder article number spalte nehmen je nachdem was befüllt 
    private ArticleInfo resolveArticle(MessageLogistic msg) {
        if (msg.getArticleId() != null) {
            return articleRepo.findByArticleId(msg.getArticleId());
        }

        if (msg.getArticleNumber() != null && !msg.getArticleNumber().isBlank()) {
            try {
                Long fallbackArticleId = Long.valueOf(msg.getArticleNumber());
                return articleRepo.findByArticleId(fallbackArticleId);
            } catch (NumberFormatException e) {
                return articleRepo.findByArticleNumber(msg.getArticleNumber());
            }
        }

        return null;
    }
}