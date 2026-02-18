package de.fhdw.vendix.store.persistence.repository;

import de.fhdw.vendix.store.persistence.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByDepositRedemptionCode(String depositRedemptionCode);
}
