package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import fhdw.de.einkauf_service.dto.ArticleRequestDTO;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.query.ArticleSpecifications;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.service.ArticleService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    // ==================================================================================
    // 1. CREATE (POST)
    // ==================================================================================
    @Transactional
    @Override
    public ArticleResponseDTO createNewArticle(ArticleRequestDTO newArticleRequestDTO) {

        // 1. DTO zu Entity mappen
        Article newArticle = mapRequestToEntity(newArticleRequestDTO);

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
    @Override
    public ArticleResponseDTO findArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Article with ID " + id + " not found."));

        return mapEntityToResponse(article);
    }

    // ==================================================================================
    // 3. READ (GET by Filterkriterien, get all ohne Angabe von Filtern)
    // ==================================================================================
    @Override
    public List<ArticleResponseDTO> findFilteredArticles(ArticleFilterDTO filter) {
        // 1. Abfrage durchführen
        Specification<Article> spec = ArticleSpecifications.filterArticles(filter);
        List<Article> articles = articleRepository.findAll(spec);

        // 2. Mapping HIER im Service durchführen
        return articles.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    // ==================================================================================
    // 4. UPDATE (PUT)
    // ==================================================================================
    @Transactional
    @Override
    public ArticleResponseDTO updateArticle(Long id, ArticleRequestDTO updatedArticleRequestDTO) {

        // 1. Artikel finden (Sicherstellen, dass die ID existiert)
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Article with ID " + id + " not found."));

        // 2. Felder aus dem Request DTO auf die existierende Entity übertragen
        //    Artikelnummer wird nicht aktualisiert
        existingArticle.setName(updatedArticleRequestDTO.getName());
        existingArticle.setUnit(updatedArticleRequestDTO.getUnit());
        existingArticle.setPurchasePrice(updatedArticleRequestDTO.getPurchasePrice());
        existingArticle.setTaxRatePercent(updatedArticleRequestDTO.getTaxRatePercent());
        existingArticle.setManufacturer(updatedArticleRequestDTO.getManufacturer());
        existingArticle.setSupplier(updatedArticleRequestDTO.getSupplier());
        existingArticle.setStockLevel(updatedArticleRequestDTO.getStockLevel());
        existingArticle.setDescription(updatedArticleRequestDTO.getDescription());

        // 3. Preis neu berechnen
        Double purchasePrice = updatedArticleRequestDTO.getPurchasePrice();
        Double taxRatePercent = updatedArticleRequestDTO.getTaxRatePercent();
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
    @Override
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
    private Article mapRequestToEntity(ArticleRequestDTO request) {
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
        entity.setIsAvailable(request.getIsAvailable());
        return entity;
    }

    /**
     * Konvertiert Article Entity in ArticleResponse DTO.
     * @param entity Die Entity aus der Datenbank.
     * @return Das ausgehende Response DTO.
     */
    private ArticleResponseDTO mapEntityToResponse(Article entity) {
        return new ArticleResponseDTO(
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
                entity.getDescription(),
                entity.getIsAvailable()
        );
    }

    /**
     * Ermittelt alle unterschiedlichen Lieferantennamen von derzeit verfügbaren Artikeln.
     * Wird für das Lieferanten-Dropdown im Frontend verwendet.
     * @return Alphabetisch sortierte Liste aller eindeutigen Lieferanten, die mindestens einem verfügbaren Artikel zugeordnet sind.
     */
    public List<String> findAllSupplierNames() {
        return articleRepository.findAll()
                .stream()
                .filter(Article::getIsAvailable)
                .map(Article::getSupplier)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

}