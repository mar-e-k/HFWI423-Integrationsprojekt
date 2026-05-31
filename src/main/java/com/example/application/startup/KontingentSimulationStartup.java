package com.example.application.startup;

import com.example.application.data.contingent.ContingentLasttest;
import com.example.application.data.contingent.ContingentLasttestRepository;
import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.services.MessagingEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Befüllt die ContingentLasttest-Tabelle beim App-Start automatisch,
 * damit der "Neue Artikel – Lasttest"-Tab sofort Daten enthält.
 * Läuft nur wenn die Tabelle leer ist, um Doppelbefüllung zu verhindern.
 */
@Component
@Order(2)
public class KontingentSimulationStartup implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(KontingentSimulationStartup.class);
    private static final int DEFAULT_COUNT = 100;
    private static final int CHUNK_SIZE = 500;

    private static final String[] ARTIKEL_KATEGORIEN = {
            "Bio-Vollmilch", "Haferflocken", "Mineralwasser", "Orangensaft", "Roggenbrot",
            "Camembert", "Rinderhackfleisch", "Lachsfilet", "Olivenoel", "Basmati-Reis",
            "Tomatenmark", "Griechischer Joghurt", "Cashewkerne", "Dinkelnudeln", "Erdbeerkonfituere"
    };

    private final ContingentLasttestRepository contingentLasttestRepository;
    private final MessagingEventService messagingEventService;

    public KontingentSimulationStartup(ContingentLasttestRepository contingentLasttestRepository,
                                       MessagingEventService messagingEventService) {
        this.contingentLasttestRepository = contingentLasttestRepository;
        this.messagingEventService = messagingEventService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (contingentLasttestRepository.count() > 0) {
            logger.info("ContingentLasttest-Tabelle bereits befüllt – Startup-Simulation übersprungen.");
            return;
        }

        logger.info("ContingentLasttest-Tabelle ist leer – starte automatische Simulation mit {} Einträgen.", DEFAULT_COUNT);
        Random random = new Random();
        List<ContingentLasttest> contingents = new ArrayList<>(DEFAULT_COUNT);
        List<MessagingEvent> events = new ArrayList<>(DEFAULT_COUNT);

        for (int i = 0; i < DEFAULT_COUNT; i++) {
            int menge = 10 + random.nextInt(491);
            contingents.add(buildSyntheticNewArticle(menge, random));
            events.add(new MessagingEvent("NewQuota", -(long) (i + 1), (long) menge, "Startup-Simulation"));
        }

        for (int i = 0; i < contingents.size(); i += CHUNK_SIZE) {
            contingentLasttestRepository.saveAll(contingents.subList(i, Math.min(i + CHUNK_SIZE, contingents.size())));
        }
        for (int i = 0; i < events.size(); i += CHUNK_SIZE) {
            messagingEventService.saveAll(events.subList(i, Math.min(i + CHUNK_SIZE, events.size())));
        }

        logger.info("Startup-Simulation abgeschlossen: {} SIM-Artikel in ContingentLasttest angelegt.", DEFAULT_COUNT);
    }

    private ContingentLasttest buildSyntheticNewArticle(int menge, Random random) {
        ContingentLasttest c = new ContingentLasttest();
        c.setArticleId(-(long) (random.nextInt(900_000) + 100_000));
        c.setAvailableQuantity(menge);
        String number = "SIM-" + String.format("%05d", random.nextInt(99_999) + 1);
        String name = ARTIKEL_KATEGORIEN[random.nextInt(ARTIKEL_KATEGORIEN.length)]
                + " " + (100 + random.nextInt(900)) + "g";
        c.setSimArticleNumber(number);
        c.setSimArticleName(name);
        return c;
    }
}
