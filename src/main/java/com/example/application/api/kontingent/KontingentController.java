package com.example.application.api.kontingent;

import com.example.application.data.contingent.ContingentLasttest;
import com.example.application.data.contingent.ContingentLasttestRepository;
import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.services.MessagingEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/kontingente")
@Tag(name = "Kontingente", description = "Kontingent-Simulation fuer Lasttests")
public class KontingentController {

    private static final Logger logger = LoggerFactory.getLogger(KontingentController.class);
    private static final int CHUNK_SIZE = 500;

    private final ContingentLasttestRepository contingentLasttestRepository;
    private final MessagingEventService messagingEventService;

    public KontingentController(ContingentLasttestRepository contingentLasttestRepository,
                                MessagingEventService messagingEventService) {
        this.contingentLasttestRepository = contingentLasttestRepository;
        this.messagingEventService = messagingEventService;
    }

    record SimulationResult(int requested, int created, int existingArticleMessages,
                            int newArticleMessages, int skippedNewArticle, String info) {}

    private static final String[] ARTIKEL_KATEGORIEN = {
            "Bio-Vollmilch", "Haferflocken", "Mineralwasser", "Orangensaft", "Roggenbrot",
            "Camembert", "Rinderhackfleisch", "Lachsfilet", "Olivenoel", "Basmati-Reis",
            "Tomatenmark", "Griechischer Joghurt", "Cashewkerne", "Dinkelnudeln", "Erdbeerkonfituere"
    };

    @Operation(summary = "Simulierte Kontingent-Nachrichten generieren")
    @PostMapping("/simulate")
    public SimulationResult simulate(@RequestParam(defaultValue = "5000") int count) {
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
            contingentLasttestRepository.saveAll(contingents.subList(i, Math.min(i + CHUNK_SIZE, contingents.size())));
        }
        for (int i = 0; i < events.size(); i += CHUNK_SIZE) {
            messagingEventService.saveAll(events.subList(i, Math.min(i + CHUNK_SIZE, events.size())));
        }
        logger.info("Kontingent-Simulation abgeschlossen: {} SIM-Artikel angelegt", count);
        return new SimulationResult(count, count, 0, count, 0,
                count + " synthetische neue Artikel (SIM-XXXXX) generiert.");
    }

    @Operation(summary = "Alle simulierten Kontingente loeschen")
    @DeleteMapping("/simulate")
    @Transactional
    public Map<String, Object> reset() {
        long contingentCount = contingentLasttestRepository.count();
        contingentLasttestRepository.deleteAllBulk();
        return Map.of("deletedContingents", contingentCount,
                "info", "Alle Kontingente geloescht. MessagingEvents bleiben als Audit-Log erhalten.");
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

    private MessagingEvent buildEvent(long articleId, int menge) {
        return new MessagingEvent("NewQuota", articleId, (long) menge, "Lasttest-Simulation");
    }
}
