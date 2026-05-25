package com.example.application.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RestockCheckListener {

    private static final Logger log = LoggerFactory.getLogger(RestockCheckListener.class);

    @EventListener
    public void onGoodsReceiptApproved(GoodsReceiptApprovedEvent event) {
        log.info("[Observer] Wareneingang {} abgeschlossen - Nachbestellpruefung ausgeloest", event.getReceiptId());
        // Durch den Observer-Pattern bleibt GoodsReceiptService von RestockService entkoppelt.
    }
}
