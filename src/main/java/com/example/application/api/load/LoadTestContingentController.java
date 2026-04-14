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
    @PostMapping("/simulate")
    @Transactional
    public SimulationResult simulate(
            @RequestParam(defaultValue = "5000") int count) {

        if (count <= 0 || count > 100_000) {
            throw new IllegalArgumentException("count muss zwischen 1 und 100.000 liegen");
        }

        // --- Daten laden --------------------------------------------------------

        // Bekannte Artikel (haben bereits einen ArticleInfo-Eintrag)
        List<ArticleInfo> bekannteArtikel = articleInfoRepository.findAll();

        // Alle externen Artikel-IDs ermitteln, die noch KEIN ArticleInfo haben → "neue Artikel"
        Set<String> vorhandeneArtikelnummern = articleInfoRepository.findAllArticleNumbers();
        List<ExternalArticle> alleExternen = externalArticleRepository.findAll();
        List<ExternalArticle> neueExterneArtikel = alleExternen.stream()
                .filter(ea -> !vorhandeneArtikelnummern.contains(ea.getArticleNumber()))
                .collect(Collectors.toList());

        // --- Ziel-Aufteilung berechnen ------------------------------------------

        int zielNeu = (int) Math.round(count * NEW_ARTICLE_RATIO);
        int zielBekannt = count - zielNeu;

        // Wenn zu wenige neue Artikel vorhanden → so viele wie möglich, Rest auf bekannte
        int tatsaechlichNeu = Math.min(zielNeu, neueExterneArtikel.size());
        int uebernahmeAufBekannt = zielNeu - tatsaechlichNeu; // überbleibende 5%-Slots
        int tatsaechlichBekannt = zielBekannt + uebernahmeAufBekannt;

        if (bekannteArtikel.isEmpty() && tatsaechlichNeu == 0) {
            return new SimulationResult(count, 0, 0, 0, zielNeu - tatsaechlichNeu,
                    "Keine Artikel (weder bekannte noch neue) vorhanden – Simulation abgebrochen.");
        }

        logger.info("Starte Kontingent-Simulation: {} Nachrichten ({} bekannt, {} neu)",
                count, tatsaechlichBekannt, tatsaechlichNeu);

        Random random = new Random();
        List<ContingentLasttest> contingents = new ArrayList<>(count);
        List<MessagingEvent> events = new ArrayList<>(count);

        // --- Nachrichten für bekannte Artikel generieren ------------------------

        if (!bekannteArtikel.isEmpty()) {
            for (int i = 0; i < tatsaechlichBekannt; i++) {
                ArticleInfo artikel = bekannteArtikel.get(random.nextInt(bekannteArtikel.size()));
                long articleId = artikel.getArticleId() != null
                        ? artikel.getArticleId()
                        : artikel.getId(); // Fallback auf interne ID
                int menge = 10 + random.nextInt(491); // 10–500 Stück

                contingents.add(buildContingent(articleId, menge));
                events.add(buildEvent(articleId, menge));
            }
        }

        // --- Nachrichten für neue Artikel generieren ----------------------------

        // Zufällige Auswahl aus den verfügbaren neuen Artikeln (ohne Wiederholung bei wenigen)
        Collections.shuffle(neueExterneArtikel, random);
        for (int i = 0; i < tatsaechlichNeu; i++) {
            // Wenn mehr Slots als neue Artikel: Index per Modulo wiederholen
            ExternalArticle ext = neueExterneArtikel.get(i % neueExterneArtikel.size());
            int menge = 10 + random.nextInt(491);

            contingents.add(buildContingent(ext.getId(), menge));
            events.add(buildEvent(ext.getId(), menge));
        }

        // --- Batch-Speicherung --------------------------------------------------

        contingentLasttestRepository.saveAll(contingents);

        // MessagingEventService hat kein saveAll → direkt über das Repository gehen
        // geht nicht ohne Zugriff auf das Repo, daher saveAll-Loop in Chunks
        int chunkSize = 500;
        for (int i = 0; i < events.size(); i += chunkSize) {
            List<MessagingEvent> chunk = events.subList(i, Math.min(i + chunkSize, events.size()));
            for (MessagingEvent ev : chunk) {
                messagingEventService.save(ev);
            }
        }

        int erstellt = contingents.size();
        logger.info("Kontingent-Simulation abgeschlossen: {} Datensätze angelegt", erstellt);

        return new SimulationResult(
                count,
                erstellt,
                Math.min(tatsaechlichBekannt, bekannteArtikel.isEmpty() ? 0 : tatsaechlichBekannt),
                tatsaechlichNeu,
                zielNeu - tatsaechlichNeu,
                tatsaechlichNeu < zielNeu
                        ? "Nur " + neueExterneArtikel.size() + " neue Artikel verfügbar – fehlende Slots auf bekannte Artikel umgeleitet."
                        : "Simulation erfolgreich."
        );
    }

    /**
     * DELETE /api/load/contingents/simulate – alle simulierten Kontingente und Events löschen
     */
    @DeleteMapping("/simulate")
    @Transactional
    public Map<String, Object> reset() {
        long contingentCount = contingentLasttestRepository.count();
        contingentLasttestRepository.deleteAll();

        return Map.of(
                "deletedContingents", contingentCount,
                "info", "Alle Kontingente gelöscht. MessagingEvents bleiben als Audit-Log erhalten."
        );
    }

    // -------------------------------------------------------------------------

    private ContingentLasttest buildContingent(long articleId, int menge) {
        ContingentLasttest c = new ContingentLasttest();
        c.setArticleId(articleId);
        c.setAvailableQuantity(menge);
        // OrderId und SupplierId werden für die Simulation nicht gesetzt
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
