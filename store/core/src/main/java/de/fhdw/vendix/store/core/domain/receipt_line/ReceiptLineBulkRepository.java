package de.fhdw.vendix.store.core.domain.receipt_line;

import java.util.List;

/**
 * Masseneinfügung von {@link ReceiptLine}-Entitäten via JDBC-Batch.
 *
 * <p>Hintergrund: Hibernate verwendet intern {@code GenerationType.IDENTITY},
 * was bedeutet, dass die Datenbank die ID nach jedem einzelnen INSERT
 * zurückgeben muss. Hibernate kann deshalb keine nativen JDBC-Batches für
 * INSERTs bilden — jede Entität wird einzeln gespeichert.
 *
 * <p>Diese Schnittstelle umgeht Hibernate vollständig und schreibt direkt
 * via {@link org.springframework.jdbc.core.JdbcTemplate#batchUpdate} in die
 * Datenbank. Das reduziert bei 600 Positionen von 600 einzelnen Roundtrips
 * auf einen einzigen Batch-Aufruf.
 *
 * <p>Die Audit-Felder ({@code created_at}, {@code created_by} etc.) werden
 * von der Implementierung gesetzt, da der JPA-{@code AuditingEntityListener}
 * bei direkten JDBC-Zugriffen nicht aktiv ist.
 */
public interface ReceiptLineBulkRepository {

    /**
     * Fügt alle übergebenen {@link ReceiptLine}-Objekte in einem einzigen
     * JDBC-Batch ein und gibt sie mit den von der Datenbank vergebenen IDs
     * zurück.
     *
     * @param lines nicht-leere Liste von Positionen (ohne ID)
     * @return dieselben Positionen, angereichert mit DB-IDs
     */
    List<ReceiptLine> bulkInsert(List<ReceiptLine> lines);
}
