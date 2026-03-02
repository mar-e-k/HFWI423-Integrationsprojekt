package de.fhdw.vendix.store.core.domain.receipt_line;

import de.fhdw.vendix.store.core.domain.AbstractCrudService;
import de.fhdw.vendix.store.core.domain.receipt.Receipt;
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