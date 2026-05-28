package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.checkout.CheckoutLineDTO;
import de.fhdw.vendix.commons.api.domain.checkout.CheckoutRequestDTO;
import de.fhdw.vendix.commons.api.domain.checkout.CheckoutResponseDTO;
import de.fhdw.vendix.commons.api.embeddable.DiscountOverrideDTO;
import de.fhdw.vendix.commons.api.embeddable.OverrideReason;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLineBulkRepository;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLineMapper;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import de.fhdw.vendix.store.core.domain.store_stock.StoreStockService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Erstellt einen vollständigen Bon in einer einzigen Datenbank-Transaktion.
 *
 * <h3>Bulk-Insert-Optimierung</h3>
 * <p>Früher wurden alle {@code ReceiptLine}-Entitäten einzeln über
 * {@code receiptLineService.create()} gespeichert — das ergab bei 600 Artikel-
 * Positionen 600 einzelne SQL-INSERTs mit je einem Datenbank-Roundtrip.
 *
 * <p>Jetzt delegiert die Methode an {@link ReceiptLineBulkRepository#bulkInsert},
 * das via {@code JdbcTemplate.batchUpdate()} alle Positionen in einem einzigen
 * PreparedStatement-Batch schreibt. Das reduziert den Overhead drastisch:
 *
 * <pre>
 * vorher:  600 INSERT-Statements × N Roundtrips = hohes Latenz-Budget
 * nachher: 1  BatchUpdate        × 1 Roundtrip  = minimales Latenz-Budget
 * </pre>
 *
 * <p>Warum nicht {@code saveAll()}? Das Spring-Data-Basis-Entity verwendet
 * {@code GenerationType.IDENTITY}. Hibernate muss deshalb nach jedem INSERT
 * die vergebene ID abfragen — Batching ist damit strukturell nicht möglich.
 * {@code JdbcTemplate} umgeht Hibernate und benutzt {@code RETURN_GENERATED_KEYS},
 * um alle IDs in einem einzigen Aufruf zurückzubekommen.
 */
@Service
class CheckoutServiceImpl implements CheckoutService {

    private final ReceiptService          receiptService;
    private final ReceiptLineBulkRepository receiptLineBulkRepository;
    private final ReceiptLineMapper       receiptLineMapper;
    private final StoreStockService storeStockService;

    CheckoutServiceImpl(ReceiptService receiptService,
                        ReceiptLineBulkRepository receiptLineBulkRepository,
                        ReceiptLineMapper receiptLineMapper,
                        StoreStockService storeStockService) {
        this.receiptService             = receiptService;
        this.receiptLineBulkRepository  = receiptLineBulkRepository;
        this.receiptLineMapper          = receiptLineMapper;
        this.storeStockService          = storeStockService;
    }

    @Override
    @Transactional
    public CheckoutResponseDTO checkout(CheckoutRequestDTO request) {

        // 1. Bon-Header erstellen
        Receipt receipt = new Receipt(
                request.storeId(),
                request.registerId(),
                request.cashierUUID(),
                request.paymentMethod()
        );
        Long receiptId = Objects.requireNonNull(
                receiptService.create(receipt).getId(),
                "Receipt ID nach save() ist null — Hibernate-Fehler"
        );

        // 2. Gleiche Positionen zusammenfassen, bevor sie persistiert werden.
        //    Der Capacity-Test erzeugt oft hunderte Duplikate derselben Artikel.
        List<CheckoutLineDTO> aggregatedRequestLines = aggregateLines(request.lines());
        List<ReceiptLine> lines = new ArrayList<>(aggregatedRequestLines.size());

        for (CheckoutLineDTO line : aggregatedRequestLines) {
            DiscountOverrideDTO discount = null;
            BigDecimal discountPercent = line.discountPercent();
            if (discountPercent != null) {
                discount = new DiscountOverrideDTO(
                        discountPercent,
                        OverrideReason.PROMOTIONAL_ADJUSTMENT
                );
            }

            lines.add(receiptLineMapper.toEntity(new ReceiptLineDTO(
                    null,
                    receiptId,
                    line.articleId(),
                    line.articleAmount(),
                    discount,
                    null
            )));
        }

        // 3. Alle Positionen in einem einzigen JDBC-Batch einfügen
        //    statt N einzelner Hibernate-INSERTs
        List<ReceiptLine> savedLines = receiptLineBulkRepository.bulkInsert(lines);
        storeStockService.decrementArticles(request.storeId(), aggregateArticleAmounts(request.lines()));

        // 4. Response
        List<Long> lineIds = request.returnLineIds()
                ? savedLines.stream()
                        .map(ReceiptLine::getId)
                        .toList()
                : List.of();

        return new CheckoutResponseDTO(
                receiptId,
                request.storeId(),
                request.registerId(),
                request.cashierUUID(),
                savedLines.size(),
                lineIds
        );
    }

    private static List<CheckoutLineDTO> aggregateLines(List<CheckoutLineDTO> lines) {
        Map<LineAggregationKey, Long> amountByLine = new LinkedHashMap<>();

        for (CheckoutLineDTO line : lines) {
            LineAggregationKey key = new LineAggregationKey(line.articleId(), normalize(line.discountPercent()));
            amountByLine.merge(key, line.articleAmount(), Long::sum);
        }

        return amountByLine.entrySet().stream()
                .map(entry -> new CheckoutLineDTO(
                        entry.getKey().articleId(),
                        entry.getValue(),
                        entry.getKey().discountPercent()
                ))
                .toList();
    }

    private static Map<Long, Long> aggregateArticleAmounts(List<CheckoutLineDTO> lines) {
        Map<Long, Long> amountByArticle = new LinkedHashMap<>();
        for (CheckoutLineDTO line : lines) {
            amountByArticle.merge(line.articleId(), line.articleAmount(), Long::sum);
        }
        return amountByArticle;
    }

    private static @Nullable BigDecimal normalize(@Nullable BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros();
    }

    private record LineAggregationKey(Long articleId, @Nullable BigDecimal discountPercent) {}
}
