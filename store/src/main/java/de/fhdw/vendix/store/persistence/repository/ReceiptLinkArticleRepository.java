package de.fhdw.vendix.store.persistence.repository;

import de.fhdw.vendix.store.persistence.entity.Receipt;
import de.fhdw.vendix.store.persistence.entity.ReceiptArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceiptLinkArticleRepository extends JpaRepository<ReceiptArticle, Long> {
    List<ReceiptArticle> findByReceipt(Receipt receipt);
}