package de.fhdw.vendix.store.core.domain.receipt;

import de.fhdw.vendix.store.core.domain.receipt_line.ReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    List<Receipt> findAllByStoreId(Long storeId);

    List<Receipt> findAllByRegisterId(Long registerId);

    List<Receipt> findAllByCashierUuid(UUID cashierUuid);

    @Query(
                    """
                    SELECT r
                    FROM Receipt r
                    WHERE r.storeId = :storeId
                      AND r.createdAt >= CURRENT_DATE
                    """
    )
    List<Receipt> findAllByStoreIdAndCreatedAtToday(@Param("storeId") Long storeId);

    @Query(
                    """
                    SELECT DISTINCT rl.articleId
                    FROM Receipt r, ReceiptLine rl
                    WHERE rl.receiptId = r.id
                      AND r.storeId = :storeId
                      AND r.createdAt >= CURRENT_DATE
                    """
    )
    List<Long> findDistinctArticleIdsSoldTodayByStoreId(@Param("storeId") Long storeId);

    @Query(
                    """
                    SELECT rl
                    FROM ReceiptLine rl
                    WHERE rl.receiptId = :receiptId
                    """
    )
    List<ReceiptLine> findAllReceiptLinesByReceiptId(@Param("receiptId") Long id);
}
