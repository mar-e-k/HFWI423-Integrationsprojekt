package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.dto.CategoryRequestDTO;
import fhdw.de.einkauf_service.dto.CategoryResponseDTO;
import fhdw.de.einkauf_service.entity.Category;
import fhdw.de.einkauf_service.repository.ArticleCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleCategoryServiceImplTest {

    @Mock
    private ArticleCategoryRepository categoryRepository;

    @InjectMocks
    private ArticleCategoryServiceImpl service;

    private Category buildCategory(Long id, String name, String description) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        c.setDescription(description);
        return c;
    }

    // --- createCategory ---

    @Test
    void createCategory_happyPath_returnsMappedDTO() {
        CategoryRequestDTO request = new CategoryRequestDTO("Getränke", "Alle Getränke");
        Category saved = buildCategory(1L, "Getränke", "Alle Getränke");

        when(categoryRepository.findByName("Getränke")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponseDTO result = service.createCategory(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Getränke");
        assertThat(result.getDescription()).isEqualTo("Alle Getränke");
    }

    @Test
    void createCategory_duplicateName_throwsIllegalArgumentException() {
        CategoryRequestDTO request = new CategoryRequestDTO("Getränke", "desc");
        when(categoryRepository.findByName("Getränke")).thenReturn(Optional.of(buildCategory(1L, "Getränke", "desc")));

        assertThatThrownBy(() -> service.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bereits");
    }

    @Test
    void createCategory_saveCalledOnce() {
        CategoryRequestDTO request = new CategoryRequestDTO("Neu", "desc");
        when(categoryRepository.findByName("Neu")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(buildCategory(1L, "Neu", "desc"));

        service.createCategory(request);

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    // --- getCategoryById ---

    @Test
    void getCategoryById_found_returnsDTO() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(buildCategory(1L, "Getränke", "desc")));

        CategoryResponseDTO result = service.getCategoryById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Getränke");
    }

    @Test
    void getCategoryById_notFound_throwsEntityNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCategoryById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // --- getAllCategories ---

    @Test
    void getAllCategories_returnsListOfDTOs() {
        when(categoryRepository.findAll()).thenReturn(List.of(
                buildCategory(1L, "A", "descA"),
                buildCategory(2L, "B", "descB")
        ));

        List<CategoryResponseDTO> result = service.getAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("A");
        assertThat(result.get(1).getName()).isEqualTo("B");
    }

    @Test
    void getAllCategories_emptyRepository_returnsEmptyList() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        assertThat(service.getAllCategories()).isEmpty();
    }

    // --- updateCategory ---

    @Test
    void updateCategory_happyPath_updatesAndReturnsMappedDTO() {
        Category existing = buildCategory(1L, "Alt", "alte Beschreibung");
        Category updated = buildCategory(1L, "Neu", "neue Beschreibung");
        CategoryRequestDTO request = new CategoryRequestDTO("Neu", "neue Beschreibung");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenReturn(updated);

        CategoryResponseDTO result = service.updateCategory(1L, request);

        assertThat(result.getName()).isEqualTo("Neu");
        assertThat(result.getDescription()).isEqualTo("neue Beschreibung");
    }

    @Test
    void updateCategory_notFound_throwsEntityNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCategory(99L, new CategoryRequestDTO("X", "Y")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // --- deleteCategory ---

    @Test
    void deleteCategory_callsDeleteById() {
        service.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }
}
