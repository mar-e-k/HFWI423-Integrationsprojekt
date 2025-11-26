package fhdw.de.einkauf_service.serviceImpl;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.CrudRepositoryService;
import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import fhdw.de.einkauf_service.dto.ArticleRequestDTO;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;
import fhdw.de.einkauf_service.dto.CategoryResponseDTO;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.entity.Category;
import fhdw.de.einkauf_service.entity.Supplier;
import fhdw.de.einkauf_service.query.ArticleSpecifications;
import fhdw.de.einkauf_service.repository.ArticleCategoryRepository;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
import fhdw.de.einkauf_service.service.ArticleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@BrowserCallable
@AnonymousAllowed
@Service
public class ArticleServiceImpl extends CrudRepositoryService<Article, Long, ArticleRepository> implements ArticleService {

    private final SupplierRepository supplierRepository;
    private final ArticleRepository articleRepository;
    private final ArticleCategoryRepository categoryRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository, SupplierRepository supplierRepository, ArticleCategoryRepository categoryRepository) {
        super(articleRepository);
        this.articleRepository = articleRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
    }

    // ==================================================================================
    // 1. CREATE (POST)
    // ==================================================================================
    @Transactional
    @Override
    @CacheEvict(value = "articleSearch", allEntries = true)
    public ArticleResponseDTO createNewArticle(ArticleRequestDTO newArticleRequestDTO) {

        Supplier supplier = supplierRepository.findById(newArticleRequestDTO.getSupplierId())
                .orElseThrow(() -> new EntityNotFoundException("Lieferant mit ID " + newArticleRequestDTO.getSupplierId() + " nicht gefunden."));

        // DTO zu Entity mappen
        Article newArticle = mapRequestToEntity(newArticleRequestDTO, supplier);

        // Validation: Check for duplicate article number
        if (articleRepository.findByArticleNumber(newArticle.getArticleNumber()).isPresent()) {
            throw new IllegalArgumentException("Article number (GTIN) already exists. Duplicates are not allowed.");
        }

        // Save and return the persisted entity, mapped back to Response DTO
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
    @Cacheable(value = "articleSearch")
    @Transactional(readOnly = true)
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
    @CacheEvict(value = "articleSearch", allEntries = true)
    public ArticleResponseDTO updateArticle(Long id, ArticleRequestDTO updatedArticleRequestDTO) {

        // Artikel finden (Sicherstellen, dass die ID existiert)
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Article with ID " + id + " not found."));

        Supplier supplier = supplierRepository.findById(updatedArticleRequestDTO.getSupplierId())
                .orElseThrow(() -> new EntityNotFoundException("Lieferant mit ID " + updatedArticleRequestDTO.getSupplierId() + " nicht gefunden."));

        // Felder aus dem Request DTO auf die existierende Entity übertragen
        //    Artikelnummer wird nicht aktualisiert
        existingArticle.setName(updatedArticleRequestDTO.getName());
        existingArticle.setPurchasePrice(updatedArticleRequestDTO.getPurchasePrice());
        existingArticle.setTaxRatePercent(updatedArticleRequestDTO.getTaxRatePercent());
        existingArticle.setSellingPrice(updatedArticleRequestDTO.getSellingPrice());
        existingArticle.setManufacturer(updatedArticleRequestDTO.getManufacturer());
        existingArticle.setSupplier(supplier);
        existingArticle.setStockLevel(updatedArticleRequestDTO.getStockLevel());
        existingArticle.setDescription(updatedArticleRequestDTO.getDescription());
        existingArticle.setIsAvailable(updatedArticleRequestDTO.getIsAvailable());
        existingArticle.setHasDeposit(updatedArticleRequestDTO.getHasDeposit());
        existingArticle.setCategories(mapCategoryIdsToEntities(updatedArticleRequestDTO.getCategoryIds()));
        existingArticle.setProductImage(updatedArticleRequestDTO.getProductImage());
        existingArticle.setExpirationDate(updatedArticleRequestDTO.getExpirationDate());
        existingArticle.setWidthCm(updatedArticleRequestDTO.getWidthCm());
        existingArticle.setHeightCm(updatedArticleRequestDTO.getHeightCm());
        existingArticle.setDepthCm(updatedArticleRequestDTO.getDepthCm());


        // Speichern und Entity zu Response DTO mappen
        Article savedArticle = articleRepository.save(existingArticle);
        return mapEntityToResponse(savedArticle);
    }

    // ==================================================================================
    // 5. DELETE (DELETE)
    // ==================================================================================
    @Transactional
    @Override
    @CacheEvict(value = "articleSearch", allEntries = true)
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
    private Article mapRequestToEntity(ArticleRequestDTO request, Supplier supplier) {
        Article entity = new Article();
        entity.setArticleNumber(request.getArticleNumber());
        entity.setName(request.getName());
        entity.setPurchasePrice(request.getPurchasePrice());
        entity.setTaxRatePercent(request.getTaxRatePercent());
        entity.setSellingPrice(request.getSellingPrice());
        entity.setManufacturer(request.getManufacturer());
        entity.setSupplier(supplier);
        entity.setStockLevel(request.getStockLevel());
        entity.setDescription(request.getDescription());
        // kein setIsAvailable, da Standardwert false ist, soll bei Erstellung nicht gesetzt werden dürfen
        // → muss dann manuell nochmal auf true gesetzt werden
        entity.setHasDeposit(request.getHasDeposit());
        entity.setCategories(mapCategoryIdsToEntities(request.getCategoryIds()));
        entity.setProductImage(request.getProductImage());
        entity.setExpirationDate(request.getExpirationDate());
        entity.setWidthCm(request.getWidthCm());
        entity.setHeightCm(request.getHeightCm());
        entity.setDepthCm(request.getDepthCm());


        return entity;
    }

    /**
     * Konvertiert Article Entity in ArticleResponse DTO.
     * @param entity Die Entity aus der Datenbank.
     * @return Das ausgehende Response DTO.
     */
    private ArticleResponseDTO mapEntityToResponse(Article entity) {

        ArticleResponseDTO dto = new ArticleResponseDTO();

        dto.setId(entity.getId());
        dto.setArticleNumber(entity.getArticleNumber());
        dto.setName(entity.getName());
        dto.setPurchasePrice(entity.getPurchasePrice());
        dto.setTaxRatePercent(entity.getTaxRatePercent());
        dto.setSellingPrice(entity.getSellingPrice());
        dto.setManufacturer(entity.getManufacturer());
        dto.setStockLevel(entity.getStockLevel());
        dto.setDescription(entity.getDescription());
        dto.setIsAvailable(entity.getIsAvailable());
        dto.setHasDeposit(entity.getHasDeposit());
        dto.setWidthCm(entity.getWidthCm());
        dto.setHeightCm(entity.getHeightCm());
        dto.setDepthCm(entity.getDepthCm());

        // --- Logik für den Supplier (Relation) ---
        Supplier supplier = entity.getSupplier();

        if (supplier != null) {
            dto.setSupplierId(supplier.getId());
            dto.setSupplierName(supplier.getName());
        } else {
            dto.setSupplierName("-");
            dto.setSupplierId(null);
        }
        dto.setCategoryIds(mapCategoriesToResponseDTOs(entity.getCategories()));
        dto.setProductImage(entity.getProductImage());
        dto.setExpirationDate(entity.getExpirationDate());
        dto.setDateCreated(entity.getDateCreated());

        return dto;
    }

    private Set<Category> mapCategoryIdsToEntities(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            // Sicherstellen, dass ein leeres Set zurückgegeben wird, nicht null
            return Collections.emptySet();
        }

        // findAllById ruft alle Entitäten in einem Batch-Query ab
        List<Category> categories = categoryRepository.findAllById(categoryIds);

        // Prüfen, ob alle IDs gefunden wurden
        if (categories.size() != categoryIds.size()) {
            // Dies signalisiert, dass der Request ungültige IDs enthielt
            Set<Long> foundIds = categories.stream().map(Category::getId).collect(Collectors.toSet());

            // Findet die fehlenden IDs für eine bessere Fehlermeldung
            Set<Long> missingIds = new HashSet<>(categoryIds);
            missingIds.removeAll(foundIds);

            throw new IllegalArgumentException(
                    "One or more category IDs are invalid or do not exist: " + missingIds
            );
        }

        return new HashSet<>(categories);
    }

    private CategoryResponseDTO mapCategoryToResponseDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }

    private Set<CategoryResponseDTO> mapCategoriesToResponseDTOs(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptySet();
        }

        // Streamen, Mappen und Sammeln in einem Set
        return categories.stream()
                .map(this::mapCategoryToResponseDTO)
                .collect(Collectors.toSet());
    }
}