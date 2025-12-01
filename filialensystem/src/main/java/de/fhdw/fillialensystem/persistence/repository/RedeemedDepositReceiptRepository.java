package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.RedeemedDepositReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RedeemedDepositReceiptRepository extends JpaRepository<RedeemedDepositReceipt, Long> {
    boolean existsByReceiptId(Long receiptId);
}
