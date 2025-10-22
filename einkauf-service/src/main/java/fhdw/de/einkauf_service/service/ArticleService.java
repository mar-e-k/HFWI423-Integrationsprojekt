package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.model.Article;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public List<Article> findAllArticles() {
        return articleRepository.findAll();
    }

    @Transactional
    public Article updateArticle(Long id, Article updatedArticle) {
        // 1. Artikel finden, falls nicht vorhanden, Exception werfen (404-Fall)
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Article with ID " + id + " not found."));

        // 2. Felder aktualisieren (GTIN/articleNumber sollte nicht geändert werden)
        existingArticle.setName(updatedArticle.getName());
        existingArticle.setUnit(updatedArticle.getUnit());
        existingArticle.setPurchasePrice(updatedArticle.getPurchasePrice());
        existingArticle.setTaxRatePercent(updatedArticle.getTaxRatePercent());
        existingArticle.setManufacturer(updatedArticle.getManufacturer());
        existingArticle.setSupplier(updatedArticle.getSupplier());
        existingArticle.setStockLevel(updatedArticle.getStockLevel());
        existingArticle.setDescription(updatedArticle.getDescription());

        // 3. Preis neu berechnen (da PurchasePrice oder TaxRate sich geändert haben könnte)
        double newSellingPrice = updatedArticle.getPurchasePrice() * (1 + (updatedArticle.getTaxRatePercent() / 100.0));
        existingArticle.setSellingPrice(newSellingPrice);

        // 4. Speichern (JpaRepository.save() aktualisiert, wenn die ID gesetzt ist)
        return articleRepository.save(existingArticle);
    }

    @Transactional
    public void deleteArticle(Long id) {
        // Optional: Prüfen, ob der Artikel existiert (für saubere 404-Antwort)
        if (!articleRepository.existsById(id)) {
            throw new NoSuchElementException("Article with ID " + id + " not found.");
        }
        articleRepository.deleteById(id);
    }
}