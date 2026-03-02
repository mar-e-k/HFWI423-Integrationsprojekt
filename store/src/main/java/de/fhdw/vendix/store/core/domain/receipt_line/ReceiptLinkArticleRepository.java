package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.store.core.domain.receipt.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceiptLinkArticleRepository extends JpaRepository<ReceiptArticle, Long> {
    List<ReceiptArticle> findByReceipt(Receipt receipt);
}