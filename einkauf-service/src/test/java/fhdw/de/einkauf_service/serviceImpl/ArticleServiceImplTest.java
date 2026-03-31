package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import fhdw.de.einkauf_service.dto.ArticleRequestDTO;
import fhdw.de.einkauf_service.dto.ArticleResponseDTO;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.entity.Category;
import fhdw.de.einkauf_service.entity.Supplier;
import fhdw.de.einkauf_service.repository.ArticleCategoryRepository;
import fhdw.de.einkauf_service.repository.ArticleRepository;
import fhdw.de.einkauf_service.repository.SupplierRepository;
import fhdw.de.einkauf_service.view.SupplierView;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ArticleCategoryRepository categoryRepository;

    @Mock
    private SupplierView supplierView;

    // Manual instantiation — ArticleServiceImpl extends CrudRepositoryService which calls super(repo)
    private ArticleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ArticleServiceImpl(articleRepository, supplierRepository, categoryRepository, supplierView);
    }

    // --- Helpers ---

    private Supplier buildSupplier(Long id) {
        Supplier s = new Supplier();
        s.setId(id);
        s.setName("Lieferant-" + id);
        s.setContactPeople(new HashSet<>());
        return s;
    }

    private Category buildCategory(Long id) {
        Category c = new Category();
        c.setId(id);
        c.setName("Kategorie-" + id);
        return c;
    }

    private Article buildArticle(Long id, String articleNumber, Supplier mainSupplier) {
        Article a = new Article();
        a.setId(id);
        a.setArticleNumber(articleNumber);
        a.setName("Testartikel");
        a.setPurchasePrice(5.0);
        a.setMainSupplier(mainSupplier);
        a.setSuppliers(new HashSet<>(mainSupplier != null ? Set.of(mainSupplier) : Set.of()));
        a.setCategories(new HashSet<>());
        return a;
    }

    private ArticleRequestDTO buildValidRequest(String articleNumber, Long mainSupplierId) {
        return new ArticleRequestDTO(
                articleNumber, "Testartikel", 5.0, 19.0, 7.0,
                "Hersteller", Set.of(mainSupplierId), mainSupplierId,
                10, "Beschreibung", true, false, Set.of(1L),
                null, null, 10.0, 5.0, 3.0
        );
    }

    // --- createNewArticle ---

    @Test
    void createNewArticle_happyPath_savesAndReturnsDTO() {
        Supplier supplier = buildSupplier(1L);
        Category category = buildCategory(1L);
        Article saved = buildArticle(1L, "12345678", supplier);

        when(supplierRepository.findAllById(Set.of(1L))).thenReturn(List.of(supplier));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(articleRepository.findByArticleNumber("12345678")).thenReturn(Optional.empty());
        when(categoryRepository.findAllById(Set.of(1L))).thenReturn(List.of(category));
        when(articleRepository.save(any(Article.class))).thenReturn(saved);

        ArticleResponseDTO result = service.createNewArticle(buildValidRequest("12345678", 1L));

        assertThat(result.getArticleNumber()).isEqualTo("12345678");
    }

    @Test
    void createNewArticle_supplierIdNotFound_throwsEntityNotFoundException() {
        // findAllById returns fewer items than requested
        when(supplierRepository.findAllById(Set.of(1L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.createNewArticle(buildValidRequest("12345678", 1L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createNewArticle_mainSupplierNotFound_throwsEntityNotFoundException() {
        Supplier supplier = buildSupplier(1L);
        Category category = buildCategory(1L);

        when(supplierRepository.findAllById(Set.of(1L))).thenReturn(List.of(supplier));
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());
        when(categoryRepository.findAllById(Set.of(1L))).thenReturn(List.of(category));

        assertThatThrownBy(() -> service.createNewArticle(buildValidRequest("12345678", 1L)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createNewArticle_mainSupplierNotInSupplierSet_throwsIllegalArgumentException() {
        Supplier supplier1 = buildSupplier(1L);
        Supplier mainSupplier = buildSupplier(2L); // different object, not in the set
        Category category = buildCategory(1L);

        // supplierIds = {1L}, mainSupplierId = 1L but findById returns supplier with id=2
        when(supplierRepository.findAllById(Set.of(1L))).thenReturn(List.of(supplier1));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mainSupplier)); // id=2 != id=1
        when(categoryRepository.findAllById(Set.of(1L))).thenReturn(List.of(category));

        assertThatThrownBy(() -> service.createNewArticle(buildValidRequest("12345678", 1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hauptlieferant");
    }

    @Test
    void createNewArticle_duplicateArticleNumber_throwsIllegalArgumentException() {
        Supplier supplier = buildSupplier(1L);
        Category category = buildCategory(1L);
        Article existing = buildArticle(99L, "12345678", supplier);

        when(supplierRepository.findAllById(Set.of(1L))).thenReturn(List.of(supplier));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(categoryRepository.findAllById(Set.of(1L))).thenReturn(List.of(category));
        when(articleRepository.findByArticleNumber("12345678")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.createNewArticle(buildValidRequest("12345678", 1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GTIN");
    }

    @Test
    void createNewArticle_invalidCategoryIds_throwsIllegalArgumentException() {
        Supplier supplier = buildSupplier(1L);

        // Exception is thrown inside mapRequestToEntity → mapCategoryIdsToEntities (before findById/findByArticleNumber)
        when(supplierRepository.findAllById(Set.of(1L))).thenReturn(List.of(supplier));
        when(categoryRepository.findAllById(Set.of(1L))).thenReturn(List.of()); // no category found

        assertThatThrownBy(() -> service.createNewArticle(buildValidRequest("12345678", 1L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- findArticleById ---

    @Test
    void findArticleById_found_returnsDTO() {
        Supplier supplier = buildSupplier(1L);
        Article article = buildArticle(1L, "12345678", supplier);

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        ArticleResponseDTO result = service.findArticleById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getArticleNumber()).isEqualTo("12345678");
    }

    @Test
    void findArticleById_notFound_throwsNoSuchElementException() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findArticleById(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    // --- findFilteredArticles ---

    @Test
    @SuppressWarnings("unchecked")
    void findFilteredArticles_returnsFilteredList() {
        Supplier supplier = buildSupplier(1L);
        Article a1 = buildArticle(1L, "11111111", supplier);
        Article a2 = buildArticle(2L, "22222222", supplier);

        when(articleRepository.findAll(any(Specification.class))).thenReturn(List.of(a1, a2));

        List<ArticleResponseDTO> result = service.findFilteredArticles(new ArticleFilterDTO());

        assertThat(result).hasSize(2);
    }

    // --- updateArticle ---

    @Test
    void updateArticle_happyPath_updatesAllFields() {
        Supplier supplier = buildSupplier(1L);
        Category category = buildCategory(1L);
        Article existing = buildArticle(1L, "12345678", supplier);
        Article updated = buildArticle(1L, "12345678", supplier);
        updated.setName("Neuer Name");

        when(articleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(supplierRepository.findAllById(Set.of(1L))).thenReturn(List.of(supplier));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(categoryRepository.findAllById(Set.of(1L))).thenReturn(List.of(category));
        when(articleRepository.save(any(Article.class))).thenReturn(updated);

        ArticleResponseDTO result = service.updateArticle(1L, buildValidRequest("12345678", 1L));

        assertThat(result.getName()).isEqualTo("Neuer Name");
    }

    @Test
    void updateArticle_notFound_throwsNoSuchElementException() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateArticle(99L, buildValidRequest("12345678", 1L)))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void updateArticle_mainSupplierNotInSet_throwsIllegalArgumentException() {
        Supplier supplier1 = buildSupplier(1L);
        Supplier otherSupplier = buildSupplier(2L);
        Article existing = buildArticle(1L, "12345678", supplier1);

        // Exception thrown before mapCategoryIdsToEntities — no category stub needed
        when(articleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(supplierRepository.findAllById(Set.of(1L))).thenReturn(List.of(supplier1));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(otherSupplier)); // id=2, not in set

        assertThatThrownBy(() -> service.updateArticle(1L, buildValidRequest("12345678", 1L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- deleteArticle ---

    @Test
    void deleteArticle_exists_callsDeleteById() {
        when(articleRepository.existsById(1L)).thenReturn(true);

        service.deleteArticle(1L);

        verify(articleRepository).deleteById(1L);
    }

    @Test
    void deleteArticle_notFound_throwsNoSuchElementException() {
        when(articleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteArticle(99L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
