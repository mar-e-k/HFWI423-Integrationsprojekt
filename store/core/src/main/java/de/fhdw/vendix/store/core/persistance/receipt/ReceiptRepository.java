package de.fhdw.vendix.store.core.persistance.receipt;

import de.fhdw.vendix.store.core.persistance.receipt_line.ReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Set<Receipt> findAllByStoreId(Long storeId);

    Set<Receipt> findAllByRegisterId(Long registerId);

    Set<Receipt> findAllByCashierId(Long cashierId);

    @Query(
                    """
                    SELECT r
                    FROM Receipt r
                    WHERE r.store.id = :storeId
                      AND r.createdAt >= CURRENT_DATE
                    """
    )
    Set<Receipt> findAllByStoreIdAndCreatedAtToday(@Param("storeId") Long storeId);

    @Query(
                    """
                    SELECT rl
                    FROM ReceiptLine rl
                    WHERE rl.receipt.id = :receiptId
                    """
    )
    Set<ReceiptLine> findAllReceiptLinesByReceiptId(@Param("receiptId") Long id);
}