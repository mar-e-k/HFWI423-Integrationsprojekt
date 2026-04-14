package com.example.application.api.load;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.articleInfo.ArticleInfoRepository;
import com.example.application.data.contingent.ContingentLasttest;
import com.example.application.data.contingent.ContingentLasttestRepository;
import com.example.application.data.externalArticle.ExternalArticle;
import com.example.application.data.externalArticle.ExternalArticleRepository;
import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.services.MessagingEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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

    private static final double NEW_ARTICLE_RATIO = 0.05; // 5 %

    private final ContingentLasttestRepository contingentLasttestRepository;
    private final ArticleInfoRepository articleInfoRepository;
    private final ExternalArticleRepository externalArticleRepository;
    private final MessagingEventService messagingEventService;

    public LoadTestContingentController(ContingentLasttestRepository contingentLasttestRepository,
                                        ArticleInfoRepository articleInfoRepository,
                                        ExternalArticleRepository externalArticleRepository,
                                        MessagingEventService messagingEventService) {
        this.contingentLasttestRepository = contingentLasttestRepository;
        this.articleInfoRepository = articleInfoRepository;
        this.externalArticleRepository = externalArticleRepository;
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

        // --- Daten laden (kurze Transaktion, Connection wird danach freigegeben) -

        List<ArticleInfo> bekannteArtikel = articleInfoRepository.findAll();
        Set<String> vorhandeneArtikelnummern = articleInfoRepository.findAllArticleNumbers();
        List<ExternalArticle> neueExterneArtikel = externalArticleRepository.findAll().stream()
                .filter(ea -> !vorhandeneArtikelnummern.contains(ea.getArticleNumber()))
                .collect(Collectors.toList());

        // --- Ziel-Aufteilung berechnen ------------------------------------------

        // 5 % immer als Ziel – egal ob echte neue Artikel vorhanden sind oder nicht
        int zielNeu        = (int) Math.round(count * NEW_ARTICLE_RATIO);
        int tatsaechlichBekannt = count - zielNeu;

        if (bekannteArtikel.isEmpty()) {
            return new SimulationResult(count, 0, 0, 0, 0,
                    "Keine bekannten Artikel vorhanden – Simulation abgebrochen.");
        }

        logger.info("Starte Kontingent-Simulation: {} Nachrichten ({} bekannt, {} neu)",
                count, tatsaechlichBekannt, zielNeu);

        // --- Datensätze generieren (im Speicher, keine DB-Connection nötig) -----

        Random random = new Random();
        List<ContingentLasttest> contingents = new ArrayList<>(count);
        List<MessagingEvent> events = new ArrayList<>(count);

        // 95 % – bekannte Artikel
        for (int i = 0; i < tatsaechlichBekannt; i++) {
            ArticleInfo artikel = bekannteArtikel.get(random.nextInt(bekannteArtikel.size()));
            long articleId = artikel.getArticleId() != null ? artikel.getArticleId() : artikel.getId();
            int menge = 10 + random.nextInt(491);
            contingents.add(buildContingent(articleId, menge));
            events.add(buildEvent(articleId, menge));
        }

        // 5 % – neue Artikel: echte bevorzugen, sonst synthetisch
        Collections.shuffle(neueExterneArtikel, random);
        for (int i = 0; i < zielNeu; i++) {
            int menge = 10 + random.nextInt(491);
            if (!neueExterneArtikel.isEmpty()) {
                ExternalArticle ext = neueExterneArtikel.get(i % neueExterneArtikel.size());
                contingents.add(buildContingent(ext.getId(), menge));
                events.add(buildEvent(ext.getId(), menge));
            } else {
                contingents.add(buildSyntheticNewArticle(menge, random));
                events.add(buildEvent(-(i + 1L), menge));
            }
        }

        // --- Chunk-weise speichern (je Chunk eigene kurze Transaktion) -----------
        // → Connection wird nach jedem Chunk freigegeben, Pool bleibt verfügbar

        for (int i = 0; i < contingents.size(); i += CHUNK_SIZE) {
            List<ContingentLasttest> chunk = contingents.subList(i, Math.min(i + CHUNK_SIZE, contingents.size()));
            contingentLasttestRepository.saveAll(chunk);
        }

        for (int i = 0; i < events.size(); i += CHUNK_SIZE) {
            List<MessagingEvent> chunk = events.subList(i, Math.min(i + CHUNK_SIZE, events.size()));
            for (MessagingEvent ev : chunk) {
                messagingEventService.save(ev);
            }
        }

        int erstellt = contingents.size();
        logger.info("Kontingent-Simulation abgeschlossen: {} Datensätze angelegt", erstellt);

        String info = neueExterneArtikel.isEmpty()
                ? zielNeu + " synthetische neue Artikel generiert (keine echten verfügbar)."
                : "Simulation erfolgreich.";

        return new SimulationResult(count, erstellt, tatsaechlichBekannt, zielNeu, 0, info);
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
