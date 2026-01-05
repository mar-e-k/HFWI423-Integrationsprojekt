package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.Receipt;
import de.fhdw.fillialensystem.persistence.entity.ReceiptArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceiptLinkArticleRepository extends JpaRepository<ReceiptArticle, Long> {
    List<ReceiptArticle> findByReceipt(Receipt receipt);
}