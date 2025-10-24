package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.dto.ArticleRequest;
import fhdw.de.einkauf_service.dto.ArticleResponse;
import fhdw.de.einkauf_service.model.Article;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    // ==================================================================================
    // 1. CREATE (POST)
    // ==================================================================================
    @Transactional
    public ArticleResponse createNewArticle(ArticleRequest newArticleRequest) {

        // 1. DTO zu Entity mappen
        Article newArticle = mapRequestToEntity(newArticleRequest);

        // 2. Validation: Check for duplicate article number
        if (articleRepository.findByArticleNumber(newArticle.getArticleNumber()).isPresent()) {
            throw new IllegalArgumentException("Article number (GTIN) already exists. Duplicates are not allowed.");
        }

        // 3. Business Logic: Calculate Selling Price
        Double purchasePrice = newArticle.getPurchasePrice();
        Double taxRatePercent = newArticle.getTaxRatePercent();
        Double sellingPrice = purchasePrice * (1 + (taxRatePercent / 100.0));
        newArticle.setSellingPrice(sellingPrice);

        // 4. Save and return the persisted entity, mapped back to Response DTO
        Article savedArticle = articleRepository.save(newArticle);
        return mapEntityToResponse(savedArticle);
    }

    // ==================================================================================
    // 2. READ (GET by ID)
    // ==================================================================================
    public ArticleResponse findArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Article with ID " + id + " not found."));

        return mapEntityToResponse(article);
    }

    // ==================================================================================
    // 3. READ ALL (GET)
    // ==================================================================================
    public List<ArticleResponse> findAllArticles() {
        return articleRepository.findAll().stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    // ==================================================================================
    // 4. UPDATE (PUT)
    // ==================================================================================
    @Transactional
    public ArticleResponse updateArticle(Long id, ArticleRequest updatedArticleRequest) {

        // 1. Artikel finden (Sicherstellen, dass die ID existiert)
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Article with ID " + id + " not found."));

        // 2. Felder aus dem Request DTO auf die existierende Entity übertragen
        //    Artikelnummer wird nicht aktualisiert
        existingArticle.setName(updatedArticleRequest.getName());
        existingArticle.setUnit(updatedArticleRequest.getUnit());
        existingArticle.setPurchasePrice(updatedArticleRequest.getPurchasePrice());
        existingArticle.setTaxRatePercent(updatedArticleRequest.getTaxRatePercent());
        existingArticle.setManufacturer(updatedArticleRequest.getManufacturer());
        existingArticle.setSupplier(updatedArticleRequest.getSupplier());
        existingArticle.setStockLevel(updatedArticleRequest.getStockLevel());
        existingArticle.setDescription(updatedArticleRequest.getDescription());

        // 3. Preis neu berechnen
        Double purchasePrice = updatedArticleRequest.getPurchasePrice();
        Double taxRatePercent = updatedArticleRequest.getTaxRatePercent();
        Double newSellingPrice = purchasePrice * (1 + (taxRatePercent / 100.0));
        existingArticle.setSellingPrice(newSellingPrice);

        // 4. Speichern und Entity zu Response DTO mappen
        Article savedArticle = articleRepository.save(existingArticle);
        return mapEntityToResponse(savedArticle);
    }

    // ==================================================================================
    // 5. DELETE (DELETE)
    // ==================================================================================
    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new NoSuchElementException("Article with ID " + id + " not found.");
        }
        articleRepository.deleteById(id);
    }

    // ==================================================================================
    // PRIVATE MAPPING METHODS (HELPER)
    // ==================================================================================

    /**
     * Konvertiert ArticleRequest DTO in Article Entity.
     * @param request Das eingehende DTO.
     * @return Die neue Article Entity.
     */
    private Article mapRequestToEntity(ArticleRequest request) {
        Article entity = new Article();
        entity.setArticleNumber(request.getArticleNumber());
        entity.setName(request.getName());
        entity.setUnit(request.getUnit());
        entity.setPurchasePrice(request.getPurchasePrice());
        entity.setTaxRatePercent(request.getTaxRatePercent());
        entity.setManufacturer(request.getManufacturer());
        entity.setSupplier(request.getSupplier());
        entity.setStockLevel(request.getStockLevel());
        entity.setDescription(request.getDescription());
        return entity;
    }

    /**
     * Konvertiert Article Entity in ArticleResponse DTO.
     * @param entity Die Entity aus der Datenbank.
     * @return Das ausgehende Response DTO.
     */
    private ArticleResponse mapEntityToResponse(Article entity) {
        return new ArticleResponse(
                entity.getId(),
                entity.getArticleNumber(),
                entity.getName(),
                entity.getUnit(),
                entity.getPurchasePrice(),
                entity.getTaxRatePercent(),
                entity.getSellingPrice(),
                entity.getManufacturer(),
                entity.getSupplier(),
                entity.getStockLevel(),
                entity.getDescription()
        );
    }
}