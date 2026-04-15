package com.example.application.api.load;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
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

    private static final double NEW_ARTICLE_RATIO = 0.50; // 50 %

    private final ContingentLasttestRepository contingentLasttestRepository;
    private final ArticleInfoRepository articleInfoRepository;
    private final MessagingEventService messagingEventService;

    public LoadTestContingentController(ContingentLasttestRepository contingentLasttestRepository,
                                        ArticleInfoRepository articleInfoRepository,
                                        MessagingEventService messagingEventService) {
        this.contingentLasttestRepository = contingentLasttestRepository;
        this.articleInfoRepository = articleInfoRepository;
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

        // --- Daten laden --------------------------------------------------------

        // Nur Artikel mit gesetzter articleId verwenden – Fallback auf getId() wuerde
        // ArticleInfo-Primaerschluessel als ExternalArticle-ID missbrauchen und faelschlich
        // Order-Test-Artikel als neue Kandidaten anzeigen.
        List<ArticleInfo> bekannteArtikel = articleInfoRepository.findAll().stream()
                .filter(a -> a.getArticleId() != null)
                .collect(java.util.stream.Collectors.toList());

        if (bekannteArtikel.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
                    "Keine bekannten Artikel mit ExternalArticle-Verknuepfung vorhanden – bitte zuerst Artikel anlegen.");
        }

        // --- Ziel-Aufteilung berechnen ------------------------------------------

        int zielNeu             = (int) Math.round(count * NEW_ARTICLE_RATIO);
        int tatsaechlichBekannt = count - zielNeu;

        logger.info("Starte Kontingent-Simulation: {} Nachrichten ({} bekannt, {} neu)",
                count, tatsaechlichBekannt, zielNeu);

        // --- Datensätze generieren (im Speicher) --------------------------------

        Random random = new Random();
        List<ContingentLasttest> contingents = new ArrayList<>(count);
        List<MessagingEvent> events = new ArrayList<>(count);

        // 95 % – bekannte Artikel (articleId ist garantiert nicht null nach dem Filter oben)
        for (int i = 0; i < tatsaechlichBekannt; i++) {
            ArticleInfo artikel = bekannteArtikel.get(random.nextInt(bekannteArtikel.size()));
            long articleId = artikel.getArticleId();
            int menge = 10 + random.nextInt(491);
            contingents.add(buildContingent(articleId, menge));
            events.add(buildEvent(articleId, menge));
        }

        // 5 % – immer synthetische neue Artikel (SIM-XXXXX), unabhängig von ExternalArticle
        for (int i = 0; i < zielNeu; i++) {
            int menge = 10 + random.nextInt(491);
            contingents.add(buildSyntheticNewArticle(menge, random));
            events.add(buildEvent(-(i + 1L), menge));
        }

        // --- Chunk-weise speichern (je Chunk eigene kurze Transaktion) -----------
        // → Connection wird nach jedem Chunk freigegeben, Pool bleibt verfügbar

        for (int i = 0; i < contingents.size(); i += CHUNK_SIZE) {
            List<ContingentLasttest> chunk = contingents.subList(i, Math.min(i + CHUNK_SIZE, contingents.size()));
            contingentLasttestRepository.saveAll(chunk);
        }

        for (int i = 0; i < events.size(); i += CHUNK_SIZE) {
            List<MessagingEvent> chunk = events.subList(i, Math.min(i + CHUNK_SIZE, events.size()));
            messagingEventService.saveAll(chunk);
        }

        int erstellt = contingents.size();
        logger.info("Kontingent-Simulation abgeschlossen: {} Datensätze angelegt ({} SIM-Artikel)",
                erstellt, zielNeu);

        return new SimulationResult(count, erstellt, tatsaechlichBekannt, zielNeu, 0,
                zielNeu + " synthetische neue Artikel (SIM-XXXXX) generiert.");
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
