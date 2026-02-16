package de.fhdw.vendix.store.persistence.service;

import de.fhdw.vendix.store.persistence.entity.Receipt;
import de.fhdw.vendix.store.persistence.entity.ReceiptArticle;
import de.fhdw.vendix.store.persistence.repository.ReceiptLinkArticleRepository;
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