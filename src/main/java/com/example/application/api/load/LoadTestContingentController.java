package com.example.application.api.load;

import com.example.application.data.contingent.ContingentLasttest;
import com.example.application.data.contingent.ContingentLasttestRepository;
import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.services.MessagingEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Lasttest-Endpunkte für Kontingent-Nachrichten-Simulation.
 * Basis-URL: /api/load/contingents
 *
 * Simuliert den Eingang von Tausenden NewQuotaEvents ohne AMQP-Broker.
 * 5 % der Nachrichten betreffen neue Artikel (noch kein ArticleInfo-Eintrag),
 * 95 % betreffen bereits bekannte Artikel.
 */
@RestController
@RequestMapping("/api/load/contingents")
public class LoadTestContingentController {

    private static final Logger logger = LoggerFactory.getLogger(LoadTestContingentController.class);

    private final ContingentLasttestRepository contingentLasttestRepository;
    private final MessagingEventService messagingEventService;

    public LoadTestContingentController(ContingentLasttestRepository contingentLasttestRepository,
                                        MessagingEventService messagingEventService) {
        this.contingentLasttestRepository = contingentLasttestRepository;
        this.messagingEventService = messagingEventService;
    }

    record SimulationResult(
            int requested,
            int created,
            int existingArticleMessages,
            int newArticleMessages,
            int skippedNewArticle,
            String info
    ) {}

    /**
     * POST /api/load/contingents/simulate?count=5000
     *
     * Erzeugt {@code count} simulierte Kontingent-Nachrichten und speichert
     * je einen Contingent- und MessagingEvent-Datensatz pro Nachricht.
     * 5 % der Nachrichten verweisen auf Artikel ohne bestehenden ArticleInfo-Eintrag.
     */
    private static final int CHUNK_SIZE = 500;

    @PostMapping("/simulate")
    public SimulationResult simulate(
            @RequestParam(defaultValue = "5000") int count) {

        if (count <= 0 || count > 100_000) {
            throw new IllegalArgumentException("count muss zwischen 1 und 100.000 liegen");
        }

        logger.info("Starte Kontingent-Simulation: {} neue SIM-Artikel", count);

        Random random = new Random();
        List<ContingentLasttest> contingents = new ArrayList<>(count);
        List<MessagingEvent> events = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int menge = 10 + random.nextInt(491);
            contingents.add(buildSyntheticNewArticle(menge, random));
            events.add(buildEvent(-(i + 1L), menge));
        }

        for (int i = 0; i < contingents.size(); i += CHUNK_SIZE) {
            List<ContingentLasttest> chunk = contingents.subList(i, Math.min(i + CHUNK_SIZE, contingents.size()));
            contingentLasttestRepository.saveAll(chunk);
        }

        for (int i = 0; i < events.size(); i += CHUNK_SIZE) {
            List<MessagingEvent> chunk = events.subList(i, Math.min(i + CHUNK_SIZE, events.size()));
            messagingEventService.saveAll(chunk);
        }

        logger.info("Kontingent-Simulation abgeschlossen: {} SIM-Artikel angelegt", count);

        return new SimulationResult(count, count, 0, count, 0,
                count + " synthetische neue Artikel (SIM-XXXXX) generiert.");
    }

    /**
     * DELETE /api/load/contingents/simulate – alle simulierten Kontingente und Events löschen
     */
    @DeleteMapping("/simulate")
    @Transactional
    public Map<String, Object> reset() {
        long contingentCount = contingentLasttestRepository.count();
        contingentLasttestRepository.deleteAllBulk();

        return Map.of(
                "deletedContingents", contingentCount,
                "info", "Alle Kontingente gelöscht. MessagingEvents bleiben als Audit-Log erhalten."
        );
    }

    // -------------------------------------------------------------------------

    private static final String[] ARTIKEL_KATEGORIEN = {
            "Bio-Vollmilch", "Haferflocken", "Mineralwasser", "Orangensaft", "Roggenbrot",
            "Camembert", "Rinderhackfleisch", "Lachsfilet", "Olivenöl", "Basmati-Reis",
            "Tomatenmark", "Griechischer Joghurt", "Cashewkerne", "Dinkelnudeln", "Erdbeerkonfitüre"
    };

    private ContingentLasttest buildContingent(long articleId, int menge) {
        ContingentLasttest c = new ContingentLasttest();
        c.setArticleId(articleId);
        c.setAvailableQuantity(menge);
        return c;
    }

    private ContingentLasttest buildSyntheticNewArticle(int menge, Random random) {
        ContingentLasttest c = new ContingentLasttest();
        // Negativer articleId → klar als synthetisch erkennbar, kein Konflikt mit echten IDs
        c.setArticleId(-(long) (random.nextInt(900_000) + 100_000));
        c.setAvailableQuantity(menge);
        // Realistische Artikeldaten
        String number = "SIM-" + String.format("%05d", random.nextInt(99_999) + 1);
        String name   = ARTIKEL_KATEGORIEN[random.nextInt(ARTIKEL_KATEGORIEN.length)]
                        + " " + (100 + random.nextInt(900)) + "g";
        c.setSimArticleNumber(number);
        c.setSimArticleName(name);
        return c;
    }

    private MessagingEvent buildEvent(long articleId, int menge) {
        return new MessagingEvent(
                "NewQuota",
                articleId,
                (long) menge,
                "Lasttest-Simulation"
        );
    }
}
