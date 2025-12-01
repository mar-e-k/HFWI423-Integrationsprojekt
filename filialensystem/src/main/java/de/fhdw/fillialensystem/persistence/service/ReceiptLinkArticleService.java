package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Receipt;
import de.fhdw.fillialensystem.persistence.entity.ReceiptLinkArticle;
import de.fhdw.fillialensystem.persistence.repository.ReceiptLinkArticleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReceiptLinkArticleService extends AbstractCrudService<ReceiptLinkArticle, Long> {

    public ReceiptLinkArticleService(ReceiptLinkArticleRepository receiptLinkArticleRepository) {
        super(receiptLinkArticleRepository);
    }

    public List<ReceiptLinkArticle> findByReceipt(Receipt receipt) {
        return ((ReceiptLinkArticleRepository) repository).findByReceipt(receipt);
    }
}