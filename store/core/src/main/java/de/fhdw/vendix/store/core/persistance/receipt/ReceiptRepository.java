package de.fhdw.vendix.store.core.persistance.receipt;

import de.fhdw.vendix.store.core.persistance.receipt_line.ReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findAllByDate(LocalDate date);

    List<Receipt> findAllByStore(long storeID);

    List<Receipt> findAllByStoreToday(long storeID);

    List<Receipt> findAllByRegister(long registerID);

    List<Receipt> findAllByCashier(long cashierID);

    Set<ReceiptLine> findLinesForReceipt(long id);
}