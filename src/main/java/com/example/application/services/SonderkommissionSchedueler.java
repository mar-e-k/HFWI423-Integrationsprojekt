package com.example.application.services;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SonderkommissionSchedueler {

    private final KommissionService kommissionService;
    private final MessageLogisticRepository msgRepo;
    private final ArticleInfoRepository articleRepo;

    public SonderkommissionSchedueler(KommissionService kommissionService,
                                      MessageLogisticRepository msgRepo,
                                      ArticleInfoRepository articleRepo) {
        this.kommissionService = kommissionService;
        this.msgRepo = msgRepo;
        this.articleRepo = articleRepo;
    }

    // Erstellt sofort eine Sonderkommission für eine Filiale
    @Transactional
    public void createSonderKommission(String storeId) {

        // Alle offenen Nachrichten dieser Filiale laden, aber nur mit gewünschter Menge > 0
        List<MessageLogistic> offeneMessages =
                msgRepo.findByStoreIdAndQuantityGreaterThanAndProcessedFalse(storeId, 0);

        // Wenn nichts offen ist, muss auch keine Sonderkommission erstellt werden
        if (offeneMessages.isEmpty()) {
            return;
        }

        // Hier sammeln wir nur die Nachrichten, die wirklich lieferbar sind
        List<MessageLogistic> lieferbareMessages = new ArrayList<>();

        for (MessageLogistic msg : offeneMessages) {

            // Passenden Artikel aus article_info suchen
            ArticleInfo article = resolveArticle(msg);

            // Wenn der Artikel nicht gefunden wird, wird die Nachricht übersprungen
            if (article == null) {
                continue;
            }

            // Wenn insgesamt kein Bestand vorhanden ist, wird der Artikel übersprungen
            if (article.getTotalStock() == null || article.getTotalStock() <= 0) {
                continue;
            }

            // Nur lieferbare Artikel kommen in die Sonderkommission
            lieferbareMessages.add(msg);
        }

        // Wenn nach der Prüfung nichts mehr übrig ist, wird nichts angelegt
        if (lieferbareMessages.isEmpty()) {
            return;
        }

        // Neue Sonderkommission für die Filiale anlegen
        //TODO: Flag auf Oberfläche hinzufügen bzw picking number anders gestalten
        Kommission k = new Kommission();
        k.setStoreId(storeId);
        k.setDate(LocalDateTime.now());
        k.setFinished(false);
        k.setOrderPickingNumber(
                kommissionService.generateNextOrderPickingNumber()
        );

        // Kommission speichern
        kommissionService.save(k);

        // Alle lieferbaren Nachrichten dieser Kommission zuordnen
        for (MessageLogistic msg : lieferbareMessages) {
            msg.setKommission(k);
            msg.setProcessed(true);
            msgRepo.save(msg);
        }

        System.out.println("Sonderkommission erstellt!");
    }

    // Sucht den passenden Artikel zur Nachricht
    private ArticleInfo resolveArticle(MessageLogistic msg) {

        // Neuer sauberer Fall: articleId ist direkt gesetzt
        if (msg.getArticleId() != null) {
            ArticleInfo article = articleRepo.findByArticleId(msg.getArticleId());
            if (article != null) {
                return article;
            }
        }

        // Alter Übergangsfall: articleId fehlt, aber die ID steckt noch als Text in articleNumber
        if (msg.getArticleNumber() != null && !msg.getArticleNumber().isBlank()) {
            try {
                Long fallbackArticleId = Long.valueOf(msg.getArticleNumber());
                ArticleInfo article = articleRepo.findByArticleId(fallbackArticleId);
                if (article != null) {
                    return article;
                }
            } catch (NumberFormatException ignored) {
                // Dann war es wohl keine Zahl, also unten normal als articleNumber versuchen
            }

            // Fallback, falls in dem Feld wirklich noch eine echte articleNumber steht
            return articleRepo.findByArticleNumber(msg.getArticleNumber());
        }

        // Wenn gar nichts passt, wird null zurückgegeben
        return null;
    }
}