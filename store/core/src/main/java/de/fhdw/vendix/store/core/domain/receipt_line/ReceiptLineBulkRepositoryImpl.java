package de.fhdw.vendix.store.core.domain.receipt_line;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-Implementierung von {@link ReceiptLineBulkRepository}.
 *
 * <p>Baut ein einziges dynamisches INSERT-Statement mit N Wertezeilen und
 * {@code RETURNING id} (PostgreSQL-spezifisch):
 *
 * <pre>
 * INSERT INTO receipt_line (receipt_id, article_id, ...)
 * VALUES (?, ?, ...), (?, ?, ...), ..., (?, ?, ...)
 * RETURNING id
 * </pre>
 *
 * Damit entsteht statt N separater INSERTs (N Roundtrips) ein einziger
 * Datenbankaufruf — unabhängig von der Anzahl der Positionen.
 */
@Repository
class ReceiptLineBulkRepositoryImpl implements ReceiptLineBulkRepository {

    private static final String COLUMNS = """
            (receipt_id, article_id, article_amount,
             discount_override_amount, discount_override_reason,
             version, created_at, created_by, changed_at, changed_by)
            """;

    private static final String ROW_PLACEHOLDER = "(?, ?, ?, ?, ?, 0, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    ReceiptLineBulkRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ReceiptLine> bulkInsert(List<ReceiptLine> lines) {
        if (lines == null || lines.isEmpty()) {
            return List.of();
        }

        Timestamp ts      = Timestamp.from(Instant.now());
        String    auditor = resolveAuditor();

        // ── SQL dynamisch aufbauen ────────────────────────────────────────
        // INSERT INTO receipt_line (...) VALUES (?,...),(?,...) RETURNING id
        StringBuilder sql = new StringBuilder("INSERT INTO receipt_line ")
                .append(COLUMNS)
                .append("VALUES ");

        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(ROW_PLACEHOLDER);
        }
        sql.append(" RETURNING id");

        // ── Parameter-Array befüllen ──────────────────────────────────────
        // 10 Parameter pro Zeile × N Zeilen
        List<Object> params = new ArrayList<>(lines.size() * 10);
        for (ReceiptLine line : lines) {
            params.add(line.getReceiptId());
            params.add(line.getArticleId());
            params.add(line.getArticleAmount());

            if (line.getDiscountOverride() != null) {
                params.add(line.getDiscountOverride().getAmount());
                params.add(line.getDiscountOverride().getReason().name());
            } else {
                params.add(null);
                params.add(null);
            }

            params.add(ts);
            params.add(auditor);
            params.add(ts);
            params.add(auditor);
        }

        // ── Ein einziger Datenbankaufruf → alle IDs zurück ────────────────
        List<Long> ids = jdbcTemplate.queryForList(
                sql.toString(),
                Long.class,
                params.toArray()
        );

        // ── Entities mit vergebenen IDs zusammenbauen ─────────────────────
        List<ReceiptLine> result = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            ReceiptLine original = lines.get(i);
            result.add(new ReceiptLine(
                    ids.get(i),
                    original.getReceiptId(),
                    original.getArticleId(),
                    original.getArticleAmount(),
                    original.getDiscountOverride(),
                    original.getPriceOverride()
            ));
        }

        return result;
    }

    private static String resolveAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            return auth.getName();
        }
        return "system";
    }
}