package de.fhdw.vendix.store.core.domain.voucher;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(UUID code);
}