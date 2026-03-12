package de.fhdw.vendix.store.core.persistance.receipt_line;

import org.springframework.data.jpa.repository.JpaRepository;

interface ReceiptLineRepository extends JpaRepository<ReceiptLine, Long> {}