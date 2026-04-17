package de.fhdw.vendix.store.web.controller;

import de.fhdw.vendix.commons.api.domain.receipt.ReceiptDTO;
import de.fhdw.vendix.commons.spring.web.server.store.api.ReceiptApi;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptAlreadyCancelledException;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptAlreadyPrintedException;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptMapper;
import de.fhdw.vendix.store.core.domain.receipt.ReceiptService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller für Bon-Operationen.
 *
 * Endpunkte:
 *   POST /api/receipt/            – Bon anlegen (über ReceiptApi-Interface)
 *   POST /api/receipt/{id}/print  – Bon drucken / abschließen (OPEN → PRINTED)
 *   POST /api/receipt/{id}/cancel – Bon stornieren (OPEN → CANCELLED)
 *
 * Status-Codes:
 *   200 – Erfolgreich durchgeführt
 *   404 – Bon nicht gefunden
 *   409 – Konflikt (Bon bereits gedruckt oder bereits storniert)
 */
@RestController
@RequestMapping("/api/receipt")
class ReceiptController implements ReceiptApi {

    private final ReceiptService receiptService;
    private final ReceiptMapper  receiptMapper;

    ReceiptController(ReceiptService receiptService, ReceiptMapper receiptMapper) {
        this.receiptService = receiptService;
        this.receiptMapper  = receiptMapper;
    }

    // ─── Bon anlegen (bestehendes Interface) ──────────────────────────────────

    @Override
    public ResponseEntity<ReceiptDTO> postReceipt(ReceiptDTO receiptDTO) {
        Receipt receipt = receiptMapper.toEntity(receiptDTO);
        Receipt created = receiptService.create(receipt);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(receiptMapper.toDTO(created));
    }

    // ─── Bondruck: OPEN → PRINTED ─────────────────────────────────────────────

    /**
     * POST /api/receipt/{id}/print
     *
     * Schließt den Bon ab — fachlich: der Bon wird gedruckt und ist danach
     * unveränderlich. Simuliert den letzten Schritt am Kassenterminal.
     *
     * 200 – Bon erfolgreich gedruckt, gibt aktualisierten ReceiptDTO zurück
     * 404 – Bon nicht gefunden
     * 409 – Bon bereits gedruckt oder storniert (kein erneuter Druck möglich)
     */
    @PostMapping("/{id}/print")
    ResponseEntity<ReceiptDTO> printReceipt(@PathVariable Long id) {
        try {
            Receipt printed = receiptService.printReceipt(id);
            return ResponseEntity.ok(receiptMapper.toDTO(printed));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ReceiptAlreadyPrintedException | ReceiptAlreadyCancelledException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // ─── Stornierung: OPEN → CANCELLED ────────────────────────────────────────

    /**
     * POST /api/receipt/{id}/cancel
     *
     * Storniert den Bon. Ein stornierter Bon kann nicht mehr gedruckt werden.
     * Ein bereits gedruckter Bon kann nicht mehr storniert werden.
     *
     * 200 – Bon erfolgreich storniert, gibt aktualisierten ReceiptDTO zurück
     * 404 – Bon nicht gefunden
     * 409 – Bon bereits storniert oder bereits gedruckt
     */
    @PostMapping("/{id}/cancel")
    ResponseEntity<ReceiptDTO> cancelReceipt(@PathVariable Long id) {
        try {
            Receipt cancelled = receiptService.cancelReceipt(id);
            return ResponseEntity.ok(receiptMapper.toDTO(cancelled));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (ReceiptAlreadyCancelledException | ReceiptAlreadyPrintedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}