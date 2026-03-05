package de.fhdw.vendix.store.core.domain.receipt_voucher;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface ReceiptVoucherRepository extends JpaRepository<ReceiptVoucher, Long> {
    Optional<ReceiptVoucher> findByCode(UUID code);
}