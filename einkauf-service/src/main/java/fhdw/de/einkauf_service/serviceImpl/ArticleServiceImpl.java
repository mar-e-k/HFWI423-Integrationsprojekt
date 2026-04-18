package fhdw.de.einkauf_service.serviceImpl;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.crud.CrudRepositoryService;
import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import fhdw.de.einkauf_service.dto.ArticleRequestDTO;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;
import fhdw.de.einkauf_service.dto.CategoryResponseDTO;
import fhdw.de.einkauf_service.dto.SupplierResponseDTO;
import fhdw.de.einkauf_service.dto.PaymentTermResponseDTO;
import fhdw.de.einkauf_service.dto.ContactPersonResponseDTO;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.entity.Category;
import fhdw.de.einkauf_service.entity.ContactPerson;
import fhdw.de.einkauf_service.entity.Supplier;
import fhdw.de.einkauf_service.metrics.MetricsRegistry;
import fhdw.de.einkauf_service.query.ArticleSpecifications;
import fhdw.de.einkauf_service.repository.ArticleCategoryRepository;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
import fhdw.de.einkauf_service.service.ArticleService;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@AnonymousAllowed
@Service
public class ArticleServiceImpl extends CrudRepositoryService<Article, Long, ArticleRepository> implements ArticleService {

    private final SupplierRepository supplierRepository;
    private final ArticleRepository articleRepository;
    private final ArticleCategoryRepository categoryRepository;
    private final MetricsRegistry metrics;

    public ArticleServiceImpl(ArticleRepository articleRepository, SupplierRepository supplierRepository, ArticleCategoryRepository categoryRepository, MetricsRegistry metrics) {
        super(articleRepository);
        this.articleRepository = articleRepository;
        this.supplierRepository = supplierRepository;
        this.categoryRepository = categoryRepository;
        this.metrics = metrics;
    }

    @Override
    @Transactional
    @CacheEvict(value = "articleSearch", allEntries = true)
    public ArticleResponseDTO createNewArticle(ArticleRequestDTO newArticleRequestDTO) {
        Timer.Sample sample = Timer.start();
        try {
            // --- 1. Supplier IDs aus DTO auslesen ---
            Set<Long> supplierIds = newArticleRequestDTO.getSupplierIds() == null
                    ? Collections.emptySet()
                    : newArticleRequestDTO.getSupplierIds();

            // --- 2. Supplier Entities aus DB laden ---
            List<Supplier> suppliersFromDb = supplierRepository.findAllById(supplierIds);
            Set<Supplier> suppliers = new HashSet<>(suppliersFromDb);

            // Prüfen ob alle IDs existieren
            if (suppliers.size() != supplierIds.size()) {
                Set<Long> foundIds = suppliers.stream()
                        .map(Supplier::getId)
                        .collect(Collectors.toSet());
                Set<Long> missingIds = new HashSet<>(supplierIds);
                missingIds.removeAll(foundIds);
                throw new EntityNotFoundException("Die folgenden Supplier IDs wurden nicht gefunden: " + missingIds);
            }

            // --- 3. DTO -> Entity mappen ---
            Article newArticle = mapRequestToEntity(newArticleRequestDTO);

            // --- 4. Hauptlieferant prüfen und setzen ---
            Supplier mainSupplier = supplierRepository.findById(newArticleRequestDTO.getMainSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Hauptlieferant mit ID " + newArticleRequestDTO.getMainSupplierId() + " nicht gefunden."
                    ));

//            if (!suppliers.contains(mainSupplier)) {
//                throw new IllegalArgumentException("Hauptlieferant muss einer der zugeordneten Supplier sein.");
//            }

            newArticle.setMainSupplier(mainSupplier);

            suppliers.remove(mainSupplier);

            // --- 5. Supplier setzen (managed Entities) ---
            newArticle.setSuppliers(suppliers);

            // --- 6. Prüfen ob Artikelnummer bereits existiert ---
            if (articleRepository.findByArticleNumber(newArticle.getArticleNumber()).isPresent()) {
                throw new IllegalArgumentException("Article number (GTIN) already exists. Duplicates are not allowed.");
            }

            // --- 7. Speichern + Rückgabe ---
            Article savedArticle = articleRepository.save(newArticle);

            // 📊 TRACKING: Artikel hinzugefügt
            metrics.articlesAdded.increment();

            return mapEntityToResponse(savedArticle);
        } catch (Exception e) {
            throw e;
        } finally {
            sample.stop(metrics.orderProcessingTime);
        }
    }


    // ==================================================================================
    // 2. READ (GET by ID)
    // ==================================================================================
    @Override
    @Transactional(readOnly = true)
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

    @Override
    @Transactional
    @CacheEvict(value = "articleSearch", allEntries = true)
    public ArticleResponseDTO updateArticle(Long id, ArticleRequestDTO updatedArticleRequestDTO) {
        Timer.Sample sample = Timer.start();
        try {
            // --- 1. Artikel aus DB holen ---
            Article existingArticle = articleRepository.findById(id)
                    .orElseThrow(() ->
                            new NoSuchElementException("Article with ID " + id + " not found.")
                    );

            // --- 2. Supplier IDs aus DTO laden ---
            Set<Long> supplierIds = updatedArticleRequestDTO.getSupplierIds() == null
                    ? Collections.emptySet()
                    : updatedArticleRequestDTO.getSupplierIds();

            List<Supplier> suppliersFromDb = supplierRepository.findAllById(supplierIds);
            Set<Supplier> suppliers = new HashSet<>(suppliersFromDb);

            if (suppliers.size() != supplierIds.size()) {
                Set<Long> foundIds = suppliers.stream().map(Supplier::getId).collect(Collectors.toSet());
                Set<Long> missingIds = new HashSet<>(supplierIds);
                missingIds.removeAll(foundIds);
                throw new EntityNotFoundException("Die folgenden Supplier IDs wurden nicht gefunden: " + missingIds);
            }

            // --- 3. Hauptlieferant prüfen und setzen ---
            Supplier mainSupplier = supplierRepository.findById(updatedArticleRequestDTO.getMainSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Hauptlieferant mit ID " + updatedArticleRequestDTO.getMainSupplierId() + " nicht gefunden."
                    ));

//            if (!suppliers.contains(mainSupplier)) {
//                throw new IllegalArgumentException("Hauptlieferant muss einer der zugeordneten Supplier sein.");
//            }

            existingArticle.setMainSupplier(mainSupplier);

            // --- 4. Felder aus DTO übertragen ---
            existingArticle.setName(updatedArticleRequestDTO.getName());
            existingArticle.setPurchasePrice(updatedArticleRequestDTO.getPurchasePrice());
            existingArticle.setTaxRatePercent(updatedArticleRequestDTO.getTaxRatePercent());
            existingArticle.setSellingPrice(updatedArticleRequestDTO.getSellingPrice());
            existingArticle.setManufacturer(updatedArticleRequestDTO.getManufacturer());
            existingArticle.setStockLevel(updatedArticleRequestDTO.getStockLevel());
            existingArticle.setDescription(updatedArticleRequestDTO.getDescription());
            existingArticle.setAvailable(updatedArticleRequestDTO.getAvailable());
            existingArticle.setHasDeposit(updatedArticleRequestDTO.getHasDeposit());
            existingArticle.setCategories(mapCategoryIdsToEntities(updatedArticleRequestDTO.getCategoryIds()));
            existingArticle.setProductImage(updatedArticleRequestDTO.getProductImage());
            existingArticle.setExpirationDate(updatedArticleRequestDTO.getExpirationDate());
            existingArticle.setWidthCm(updatedArticleRequestDTO.getWidthCm());
            existingArticle.setHeightCm(updatedArticleRequestDTO.getHeightCm());
            existingArticle.setDepthCm(updatedArticleRequestDTO.getDepthCm());

            // --- 5. Supplier-Relation aktualisieren ---

            suppliers.remove(mainSupplier);
            existingArticle.setSuppliers(suppliers);

            // --- 6. Speichern & zurückgeben ---
            Article savedArticle = articleRepository.save(existingArticle);

            // 📊 TRACKING: Artikel aktualisiert
            metrics.articlesUpdated.increment();

            return mapEntityToResponse(savedArticle);
        } catch (Exception e) {
            throw e;
        } finally {
            sample.stop(metrics.orderProcessingTime);
        }
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
        
        // 📊 TRACKING: Artikel gelöscht
        metrics.articlesDeleted.increment();
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
        entity.setPurchasePrice(request.getPurchasePrice());
        entity.setTaxRatePercent(request.getTaxRatePercent());
        entity.setSellingPrice(request.getSellingPrice());
        entity.setManufacturer(request.getManufacturer());
        entity.setStockLevel(request.getStockLevel());
        entity.setDescription(request.getDescription());
        // kein setIsAvailable, da Standardwert false ist, soll bei Erstellung nicht gesetzt werden dürfen
        // → muss dann manuell nochmal auf true gesetzt werden
        // TESTEN: wieder eingefügt
        entity.setAvailable(request.getAvailable());
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

        // Basis-Felder
        dto.setId(entity.getId());
        dto.setArticleNumber(entity.getArticleNumber());
        dto.setName(entity.getName());
        dto.setPurchasePrice(entity.getPurchasePrice());
        dto.setTaxRatePercent(entity.getTaxRatePercent());
        dto.setSellingPrice(entity.getSellingPrice());
        dto.setManufacturer(entity.getManufacturer());
        dto.setStockLevel(entity.getStockLevel());
        dto.setDescription(entity.getDescription());
        dto.setAvailable(entity.getAvailable());
        dto.setHasDeposit(entity.getHasDeposit());
        dto.setWidthCm(entity.getWidthCm());
        dto.setHeightCm(entity.getHeightCm());
        dto.setDepthCm(entity.getDepthCm());
        dto.setProductImage(entity.getProductImage());
        dto.setExpirationDate(entity.getExpirationDate());
        dto.setDateCreated(entity.getDateCreated());

        // --- Suppliers (Many-to-Many) ---
        if (entity.getSuppliers() != null && !entity.getSuppliers().isEmpty()) {
            dto.setSuppliers(
                    entity.getSuppliers()
                            .stream()
                            .map(this::mapSupplierToResponseDTO)
                            .collect(Collectors.toSet()) // Set statt List
            );
        } else {
            dto.setSuppliers(Collections.emptySet());
        }

        // --- Hauptlieferant ---
        if (entity.getMainSupplier() != null) {
            dto.setMainSupplier(mapSupplierToResponseDTO(entity.getMainSupplier()));
        }

        // --- Kategorien ---
        dto.setCategoryIds(mapCategoriesToResponseDTOs(entity.getCategories()));

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

    private SupplierResponseDTO mapSupplierToResponseDTO(Supplier supplier) {

        SupplierResponseDTO dto = new SupplierResponseDTO();

        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setStreet(supplier.getStreet());
        dto.setHouseNumber(supplier.getHouseNumber());
        dto.setZip(supplier.getZip());
        dto.setCity(supplier.getCity());
        dto.setCountry(supplier.getCountry());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setActive(supplier.getActive());

        // Payment Term
        if (supplier.getPaymentTerm() != null) {
            PaymentTermResponseDTO ptDTO = new PaymentTermResponseDTO();
            ptDTO.setId(supplier.getPaymentTerm().getId());
            ptDTO.setDefinition(supplier.getPaymentTerm().getDefinition());
            ptDTO.setDescription(supplier.getPaymentTerm().getDescription());

            dto.setPaymentTerm(ptDTO);
            dto.setPaymentTermId(ptDTO.getId());
            dto.setPaymentTermDefinition(ptDTO.getDefinition());
            dto.setPaymentTermDescription(ptDTO.getDescription());
        }

        // Contact People
        if (supplier.getContactPeople() != null) {
            dto.setContactPeople(
                    supplier.getContactPeople()
                            .stream()
                            .map(this::mapContactPersonToResponseDTO)
                            .toList()
            );
        }

        return dto;
    }

    private ContactPersonResponseDTO mapContactPersonToResponseDTO(ContactPerson cp) {
        ContactPersonResponseDTO dto = new ContactPersonResponseDTO();
        dto.setId(cp.getId());
        dto.setFirstName(cp.getFirstName());
        dto.setLastName(cp.getLastName());
        dto.setRole(cp.getRole());
        dto.setPhone(cp.getPhone());
        dto.setEmail(cp.getEmail());
        return dto;
    }
}

