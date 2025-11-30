package de.fhdw.fillialensystem.persistence.service;

import de.fhdw.fillialensystem.persistence.entity.Account;
import de.fhdw.fillialensystem.persistence.entity.Receipt;
import de.fhdw.fillialensystem.persistence.entity.ReceiptLinkArticle;
import de.fhdw.fillialensystem.persistence.entity.Register;
import de.fhdw.fillialensystem.persistence.repository.AccountRepository;
import de.fhdw.fillialensystem.persistence.repository.ReceiptLinkArticleRepository;
import de.fhdw.fillialensystem.persistence.repository.ReceiptRepository;
import de.fhdw.fillialensystem.persistence.repository.RegisterRepository;
import de.fhdw.fillialensystem.persistence.repository.imported.ArticleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReceiptService extends AbstractCrudService<Receipt, Long> {

    private final ReceiptLinkArticleRepository receiptLinkArticleRepository;
    private final RegisterRepository registerRepository;
    private final AccountRepository accountRepository;
    private final ArticleRepository articleRepository;

    public ReceiptService(ReceiptRepository receiptRepository, ReceiptLinkArticleRepository receiptLinkArticleRepository, RegisterRepository registerRepository, AccountRepository accountRepository, ArticleRepository articleRepository) {
        super(receiptRepository);
        this.receiptLinkArticleRepository = receiptLinkArticleRepository;
        this.registerRepository = registerRepository;
        this.accountRepository = accountRepository;
        this.articleRepository = articleRepository;
    }


    @Transactional
    public Receipt createReceipt(Long registerId, String uuid, List<ReceiptLinkArticle> articles) {
        Register register = registerRepository.findById(registerId)
                .orElseThrow(EntityNotFoundException::new);

        Account account = accountRepository.findByUuid(uuid)
                .orElseThrow(EntityNotFoundException::new);

        if (articles.isEmpty()) {
            throw new IllegalArgumentException("No articles specified");
        }

        Receipt receipt = repository.save(new Receipt(
                register.getStore(),
                register,
                account,
                null));


        for (ReceiptLinkArticle article : articles) {
            receiptLinkArticleRepository.save(new ReceiptLinkArticle(
                    receipt,
                    articleRepository.getReferenceById(article.getId()),
                    article.getPrice(),
                    article.getAmount(),
                    article.getTaxRate(),
                    article.getOverridePrice(),
                    article.getOverrideReason(),
                    article.getDiscountedByPercent()));
        }

        return receipt;
    }
}
