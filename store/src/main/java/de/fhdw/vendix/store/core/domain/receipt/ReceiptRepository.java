package de.fhdw.vendix.store.core.domain.receipt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Optional<Receipt> findByDepositRedemptionCode(String depositRedemptionCode);
}