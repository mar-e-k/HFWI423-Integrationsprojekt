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
    @Scheduled(cron = "1 * * * * *")
    public void createWeeklyKommissionen() {

        //Alle Stores, die unverarbeitete Messages haben
        List<String> stores = msgRepo.findDistinctStoresWithUnprocessed();

        for (String storeId : stores) {

            //Alle unprocessed Messages für diesen Store laden
            List<MessageLogistic> artikel =
                    msgRepo.findByStoreIdAndQuantityGreaterThanAndProcessedFalse(storeId, 0);

            //Wenn KEINE Artikel – dann nichts machen
            if (artikel.isEmpty()) {
                continue;
            }

            //Neue Kommission initialisiern
            Kommission k = new Kommission();
            k.setStoreId(storeId);
            k.setDate(LocalDateTime.now());
            k.setFinished(false);
            k.setOrderPickingNumber(
                    kommissionService.generateNextOrderPickingNumber()
            );

            kommissionService.save(k);

            //Alle Messages dem Auftrag zuordnen
            for (MessageLogistic msg : artikel) {
                msg.setKommission(k);
                msg.setQuantity(msg.getQuantity());
                msg.setProcessed(true);
                msgRepo.save(msg);
            }

        }

        System.out.println("Wöchentliche Kommissionen erstellt.");
    }
}
