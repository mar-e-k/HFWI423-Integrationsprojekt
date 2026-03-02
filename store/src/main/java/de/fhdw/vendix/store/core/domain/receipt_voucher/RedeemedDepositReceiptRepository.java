package de.fhdw.vendix.store.core.domain.receipt_voucher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedeemedDepositReceiptRepository extends JpaRepository<RedeemedDepositReceipt, Long> {
    boolean existsByReceiptId(Long receiptId);
}
