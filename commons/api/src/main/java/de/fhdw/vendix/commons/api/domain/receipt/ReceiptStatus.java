package de.fhdw.vendix.commons.api.domain.receipt;

/**
 * Fachlicher Lebenszyklus eines Bons.
 *
 *  OPEN      → Bon wurde erstellt und kann noch bearbeitet werden
 *  PRINTED   → Bon wurde gedruckt und abgeschlossen (Endstatus, unveränderlich)
 *  CANCELLED → Bon wurde storniert (Endstatus, unveränderlich)
 *
 * Erlaubte Übergänge:
 *   OPEN → PRINTED   (Bondruck)
 *   OPEN → CANCELLED (Stornierung)
 */
public enum ReceiptStatus {
    OPEN,
    PRINTED,
    CANCELLED
}