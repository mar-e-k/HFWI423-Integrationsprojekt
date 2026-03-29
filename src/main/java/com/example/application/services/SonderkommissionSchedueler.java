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
public class SonderkommissionSchedueler {

    private final KommissionService kommissionService;
    private final MessageLogisticRepository msgRepo;

    public SonderkommissionSchedueler(KommissionService kommissionService,
                                     MessageLogisticRepository msgRepo) {
        this.kommissionService = kommissionService;
        this.msgRepo = msgRepo;
    }


    @Transactional
    public void createSonderKommission(String storeId) {

            // 2. Alle unprocessed Messages für diesen Store laden
            List<MessageLogistic> artikel =
                    msgRepo.findByStoreIdAndQuantityGreaterThanAndProcessedFalse(storeId, 0);


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

        System.out.println("Sonderkommission erstellt!");
    }
}
