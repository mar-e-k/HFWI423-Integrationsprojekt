package com.example.application.api.neueartikel;

import com.example.application.data.articleInfo.ArticleInfo;
import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.services.ArticleSyncService;
import com.example.application.services.MessagingEventService;
import com.example.application.services.NewArticleCandidate;
import com.example.application.services.StorageLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/neue-artikel")
@Tag(name = "Neue Artikel", description = "Neue Artikel aus Kontingenten anlegen")
public class NeueArtikelController {

    private final ArticleSyncService articleSyncService;
    private final MessagingEventService messagingEventService;
    private final StorageLocationService storageLocationService;

    public NeueArtikelController(ArticleSyncService articleSyncService,
                                 MessagingEventService messagingEventService,
                                 StorageLocationService storageLocationService) {
        this.articleSyncService = articleSyncService;
        this.messagingEventService = messagingEventService;
        this.storageLocationService = storageLocationService;
    }

    @Operation(summary = "Neue Artikel aus Kontingenten abrufen")
    @GetMapping
    public List<NewArticleCandidate> newArticles(
            @RequestParam(defaultValue = "false") boolean lasttest) {
        return lasttest
                ? articleSyncService.findNewArticlesFromLasttestContingents()
                : articleSyncService.findNewArticlesFromContingents();
    }

    @Operation(summary = "Naechsten neuen Artikel anlegen")
    @PostMapping("/create-next")
    public ResponseEntity<ArticleInfo> createNextNewArticle() {
        try {
            Optional<ArticleInfo> result = articleSyncService.createNextFromLasttest();
            return result.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
        } catch (Exception e) {
            return ResponseEntity.noContent().build();
        }
    }

    @Operation(summary = "Naechsten Artikel anlegen und Lagerplatz zuweisen")
    @PostMapping("/create-next-with-storage")
    public ResponseEntity<ArticleInfo> createNextNewArticleWithStorage() {
        long remaining = articleSyncService.countNewArticlesFromLasttest();
        if (remaining == 0) {
            return ResponseEntity.noContent().build();
        }
        List<StorageLocation> available = storageLocationService.findAllAvailable();
        if (available.isEmpty()) {
            if (articleSyncService.countNewArticlesFromLasttest() == 0) {
                return ResponseEntity.noContent().build();
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Keine freien Lagerplatze verfugbar - bitte zuerst Lagerplatze anlegen.");
        }
        Collections.shuffle(available);
        for (StorageLocation loc : available) {
            try {
                loc.setStorageStatus("Used");
                storageLocationService.save(loc);
                Optional<ArticleInfo> result = articleSyncService.createNextWithStorageLocation(loc.getGeneralId());
                if (result.isPresent()) {
                    return ResponseEntity.ok(result.get());
                }
                loc.setStorageStatus("Available");
                storageLocationService.save(loc);
                if (articleSyncService.countNewArticlesFromLasttest() > 0) {
                    return ResponseEntity.ok().build();
                }
                return ResponseEntity.noContent().build();
            } catch (ObjectOptimisticLockingFailureException e) {
                // anderer Thread hat diesen Lagerplatz belegt - naechsten probieren
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Keine freien Lagerplatze verfugbar - bitte zuerst Lagerplatze anlegen.");
    }

    @Operation(summary = "Alle Messaging-Events abrufen")
    @GetMapping("/messaging-events")
    public List<MessagingEvent> messagingEvents() {
        return messagingEventService.findAll();
    }
}
