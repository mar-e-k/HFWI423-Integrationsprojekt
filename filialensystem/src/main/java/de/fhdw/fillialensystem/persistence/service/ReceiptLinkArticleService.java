package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Receipt;
import de.fhdw.fillialensystem.persistence.entity.ReceiptArticle;
import de.fhdw.fillialensystem.persistence.repository.ReceiptLinkArticleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReceiptLinkArticleService extends AbstractCrudService<ReceiptArticle, Long> {

    public ReceiptLinkArticleService(ReceiptLinkArticleRepository receiptLinkArticleRepository) {
        super(receiptLinkArticleRepository);
    }

    public List<ReceiptArticle> findByReceipt(Receipt receipt) {
        return ((ReceiptLinkArticleRepository) repository).findByReceipt(receipt);
    }
}