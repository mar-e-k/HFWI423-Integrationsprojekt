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
    @Scheduled(cron = "0 20 12 * * MON")
    public void createWeeklyKommissionen() {

        List<String> stores = msgRepo.findAllStoreIds();

        for (String storeId : stores) {

            // Unterbestand (bzw > 0) prüfen
            List<MessageLogistic> messages =
                    msgRepo.findByStoreIdAndQuantityGreaterThanAndProcessedFalse(storeId, 0);
            System.out.println(messages);
            if (messages.isEmpty()) {
                continue; // Nichts zu tun
            }

            // Kommission erstellen
            Kommission k = new Kommission();
            k.setStoreId(storeId);
            k.setDate(LocalDateTime.now());
            k.setFinished(false);
            // WICHTIG → eindeutige Order-Picking-Nummer!
            k.setOrderPickingNumber(kommissionService.generateNextOrderPickingNumber());

            kommissionService.save(k);

            msgRepo.markStoreMessagesProcessed(storeId);
        }

        System.out.println("Wöchentliche Kommissionen erstellt.");
    }
}
