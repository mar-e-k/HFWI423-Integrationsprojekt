package com.example.application.services;

import com.example.application.data.orderPicking.Kommission;
import com.example.application.data.orderPicking.MessageLogisticRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    /**
     * Wird jeden Montag um 05:00 automatisch ausgelöst.
     */
    @Scheduled(cron = "0 0 5 * * MON")
    public void createWeeklyKommissionen() {

        // alle Stores holen
        List<String> stores = msgRepo.findAllStoreIds();

        for (String storeId : stores) {

            // Unterbestand für diesen Store holen
            List<?> unterbestand = msgRepo.findByStoreIdAndQuantityGreaterThan(storeId, 0);

            if (unterbestand.isEmpty()) {
                continue;   // keine Kommission nötig
            }

            // Kommission anlegen
            Kommission k = new Kommission();
            k.setStoreId(storeId);
            k.setDate(LocalDateTime.now());
            k.setFinished(false);

            kommissionService.save(k);
        }

        System.out.println("Wöchentliche Kommissionen erstellt.");
    }
}
