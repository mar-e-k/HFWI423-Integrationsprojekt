package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Receipt;
import de.fhdw.fillialensystem.persistence.entity.ReceiptLinkArticle;
import de.fhdw.fillialensystem.persistence.repository.ReceiptLinkArticleRepository;
import de.fhdw.fillialensystem.persistence.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReceiptService extends AbstractCrudService<Receipt, Long> {

    private final ReceiptLinkArticleRepository receiptLinkArticleRepository;

    public ReceiptService(ReceiptRepository receiptRepository, ReceiptLinkArticleRepository receiptLinkArticleRepository) {
        super(receiptRepository);
        this.receiptLinkArticleRepository = receiptLinkArticleRepository;
    }


    public Receipt createReceipt(Long registerId, String uuid, List<ReceiptLinkArticle> articles) {
        return null; //TODO
    }
}
