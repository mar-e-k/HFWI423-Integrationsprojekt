package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.CategoryRequestDTO;
import fhdw.de.einkauf_service.dto.CategoryResponseDTO;
import fhdw.de.einkauf_service.service.ArticleCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Verwaltung der Artikel-Kategorien")
public class CategoryController {

    private final ArticleCategoryService categoryService;

    public CategoryController(ArticleCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(summary = "Neue Kategorie anlegen", description = "Legt eine neue Artikel-Kategorie an.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kategorie erfolgreich angelegt"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabe (ProblemDetail)"),
            @ApiResponse(responseCode = "409", description = "Kategorie existiert bereits (ProblemDetail)")
    })
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO response = categoryService.createCategory(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Kategorie abrufen", description = "Liefert eine Kategorie anhand ihrer ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kategorie gefunden"),
            @ApiResponse(responseCode = "404", description = "Kategorie nicht gefunden (ProblemDetail)")
    })
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        CategoryResponseDTO response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Alle Kategorien auflisten")
    @ApiResponse(responseCode = "200", description = "Liste aller Kategorien")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> response = categoryService.getAllCategories();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Kategorie aktualisieren")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kategorie aktualisiert"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabe (ProblemDetail)"),
            @ApiResponse(responseCode = "404", description = "Kategorie nicht gefunden (ProblemDetail)")
    })
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO response = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Kategorie löschen")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Kategorie gelöscht"),
            @ApiResponse(responseCode = "404", description = "Kategorie nicht gefunden (ProblemDetail)")
    })
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
