package com.example.application.services;

import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.MessageLogistic;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WeeklyKommissionScheduler {

    private final KommissionService kommissionService;
    private final MessageLogisticRepository msgRepo;

    public WeeklyKommissionScheduler(KommissionService kommissionService,
                                     MessageLogisticRepository msgRepo) {
        this.kommissionService = kommissionService;
        this.msgRepo = msgRepo;
    }

    // Jeden Montag um 12:00
    @Transactional
    @Scheduled(cron = "1 * * * * TUE")
    public void createWeeklyKommissionen() {

        // 1. Alle Stores, die unprocessed Messages haben
        List<String> stores = msgRepo.findDistinctStoresWithUnprocessed();

        for (String storeId : stores) {

            // 2. Alle unprocessed Messages für diesen Store laden
            List<MessageLogistic> artikel =
                    msgRepo.findByStoreIdAndQuantityGreaterThanAndProcessedFalse(storeId, 0);

            // Wenn KEINE Artikel – dann nichts machen!
            if (artikel.isEmpty()) {
                continue;
            }

            // 3. Neue Kommission
            Kommission k = new Kommission();
            k.setStoreId(storeId);
            k.setDate(LocalDateTime.now());
            k.setFinished(false);
            k.setOrderPickingNumber(
                    kommissionService.generateNextOrderPickingNumber()
            );

            kommissionService.save(k);

            // 4. Alle Messages dem Auftrag zuordnen
            for (MessageLogistic msg : artikel) {
                msg.setKommission(k);
                msg.setQuantity(msg.getQuantity());
                msg.setProcessed(true);
                msgRepo.save(msg);
            }

        }

        System.out.println("Wöchentliche Kommissionen erstellt.");
    }


    /**@Transactional
    @Scheduled(cron = "0 0 6 * * *")
    public void createDailyLowStockKommissionen() {

        List<String> stores = msgRepo.findAllStoreIds();

        for (String storeId : stores) {

            // Alle Artikel des Stores laden
            List<MessageLogistic> artikel = msgRepo.findByStoreId(storeId);

            for (MessageLogistic m : artikel) {

                // Prüfen: Bestand zu niedrig?
                if (m.getQuantity() < 5) {

                    // Fehlmenge bestimmen
                    int fehlmenge = m.getTargetStockLevel() - m.getStockLevel();
                    if (fehlmenge <= 0) {
                        continue; // nichts zu tun
                    }

                    // Sonder-Kommission erzeugen
                    Kommission k = new Kommission();
                    k.setStoreId(storeId);
                    k.setDate(LocalDateTime.now());
                    k.setFinished(false);
                    k.setOrderPickingNumber(
                            kommissionService.generateNextOrderPickingNumber()
                    );

                    kommissionService.save(k);

                    // Eintrag in MessageLogistic/Positionstabelle
                    msgRepo.insertSonderArtikel(k.getId(), m.getArticleNumber(), fehlmenge);

                    System.out.println(
                            "Sonderkommission erstellt → Store " + storeId +
                                    " | Artikel " + m.getArticleNumber() +
                                    " | Fehlmenge " + fehlmenge
                    );
                }
            }
        }

        System.out.println("Tägliche Low‑Stock‑Prüfung abgeschlossen.");
    }*/
}
