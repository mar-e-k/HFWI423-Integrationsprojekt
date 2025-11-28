package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.dto.CategoryRequestDTO;
import fhdw.de.einkauf_service.dto.CategoryResponseDTO;
import fhdw.de.einkauf_service.entity.Category;
import fhdw.de.einkauf_service.repository.ArticleCategoryRepository;
import fhdw.de.einkauf_service.service.ArticleCategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleCategoryServiceImpl implements ArticleCategoryService {

    private final ArticleCategoryRepository categoryRepository;

    public ArticleCategoryServiceImpl(ArticleCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // --- Mapper-Funktionen (intern) ---

    private Category mapRequestToEntity(CategoryRequestDTO dto) {
        Category entity = new Category();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    private CategoryResponseDTO mapEntityToResponse(Category entity) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    // --- Service-Methoden (CRUD) ---

    @Transactional // Stellt sicher, dass der Vorgang atomar ist
    @CacheEvict(value = "articleSearch", allEntries = true)
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {
        categoryRepository.findByName(dto.getName())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Kategorie mit diesem Namen existiert bereits.");
                });

        Category entity = mapRequestToEntity(dto);
        Category savedEntity = categoryRepository.save(entity);
        return mapEntityToResponse(savedEntity);
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found."));
        return mapEntityToResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "articleSearch", allEntries = true)
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
        Category entity = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found."));

        // Nur die änderbaren Felder aktualisieren
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());

        Category updatedEntity = categoryRepository.save(entity);
        return mapEntityToResponse(updatedEntity);
        // HINWEIS: Da sich nur Name/Beschreibung ändern, muss nichts an den Artikeln geändert werden.
        // Die Artikel sehen die Änderung automatisch, da sie die ID referenzieren.
    }

    @Transactional
    @CacheEvict(value = "articleSearch", allEntries = true)
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
