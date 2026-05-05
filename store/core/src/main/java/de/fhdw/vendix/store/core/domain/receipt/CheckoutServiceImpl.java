package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.commons.api.domain.receipt.CheckoutLineDTO;
import de.fhdw.vendix.commons.api.domain.receipt.CheckoutRequestDTO;
import de.fhdw.vendix.commons.api.domain.receipt.CheckoutResponseDTO;
import de.fhdw.vendix.commons.api.embeddable.DiscountOverrideDTO;
import de.fhdw.vendix.commons.api.embeddable.OverrideReason;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLineBulkRepository;
import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLineMapper;
import de.fhdw.vendix.commons.api.domain.receipt_line.ReceiptLineDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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

    CheckoutServiceImpl(ReceiptService receiptService,
                        ReceiptLineBulkRepository receiptLineBulkRepository,
                        ReceiptLineMapper receiptLineMapper) {
        this.receiptService             = receiptService;
        this.receiptLineBulkRepository  = receiptLineBulkRepository;
        this.receiptLineMapper          = receiptLineMapper;
    }

    @Override
    @Transactional
    public CheckoutResponseDTO checkout(CheckoutRequestDTO request) {

        // 1. Bon-Header erstellen
        Receipt receipt = new Receipt(
                request.storeId(),
                request.registerId(),
                request.cashierId(),
                request.paymentMethod()
        );
        Long receiptId = Objects.requireNonNull(
                receiptService.create(receipt).getId(),
                "Receipt ID nach save() ist null — Hibernate-Fehler"
        );

        // 2. Alle Bon-Positionen als Entitäten vorbereiten (kein DB-Aufruf)
        List<ReceiptLine> lines = new ArrayList<>(request.lines().size());

        for (CheckoutLineDTO line : request.lines()) {

            DiscountOverrideDTO discount = null;
            if (line.discountPercent() != null) {
                discount = new DiscountOverrideDTO(
                        line.discountPercent(),
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

        // 4. Response
        List<Long> lineIds = savedLines.stream()
                .map(ReceiptLine::getId)
                .toList();

        return new CheckoutResponseDTO(
                receiptId,
                request.storeId(),
                request.registerId(),
                request.cashierId(),
                lineIds.size(),
                lineIds
        );
    }
}
