package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.model.Article;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    // Constructor Injection (best practice)
    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Transactional
    public Article createNewArticle(Article newArticle) {

        // 1. Validation: Check for duplicate article number
        if (articleRepository.findByArticleNumber(newArticle.getArticleNumber()).isPresent()) {
            throw new IllegalArgumentException("Article number (GTIN) already exists. Duplicates are not allowed.");
        }

        // 2. Business Logic: Calculate Selling Price (Acceptance Criterion)
        double purchasePrice = newArticle.getPurchasePrice();
        double taxRatePercent = newArticle.getTaxRatePercent();

        // Calculation: SellingPrice = PurchasePrice * (1 + TaxRate/100)
        double sellingPrice = purchasePrice * (1 + (taxRatePercent / 100.0));
        newArticle.setSellingPrice(sellingPrice);

        // 3. Save and return the persisted entity
        return articleRepository.save(newArticle);
    }

    // Retrieves article for visibility check (Acceptance Criterion)
    public Article findArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Article with ID " + id + " not found."));
    }
}