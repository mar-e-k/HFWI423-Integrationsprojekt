package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.ShelfRequestDTO;
import fhdw.de.einkauf_service.dto.ShelfResponseDTO;
import fhdw.de.einkauf_service.dto.ShelfLevelResponseDTO;
import fhdw.de.einkauf_service.service.ShelfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/shelves")
public class ShelfController {

    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    /**
     * Create a new shelf
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShelfResponseDTO createShelf(@Valid @RequestBody ShelfRequestDTO shelfRequestDTO) {
        return shelfService.createShelf(shelfRequestDTO);
    }

    /**
     * Get all shelves
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ShelfResponseDTO> getAllShelves() {
        return shelfService.getAllShelves();
    }

    /**
     * Get shelf by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShelfResponseDTO> getShelfById(@PathVariable Long id) {
        try {
            ShelfResponseDTO shelf = shelfService.getShelf(id);
            return ResponseEntity.ok(shelf);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all shelves for a specific category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ShelfResponseDTO>> getShelfsByCategory(@PathVariable Long categoryId) {
        List<ShelfResponseDTO> shelves = shelfService.getShelfsByCategory(categoryId);
        return ResponseEntity.ok(shelves);
    }

    /**
     * Update shelf (name and description only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShelfResponseDTO> updateShelf(@PathVariable Long id,
                                                        @Valid @RequestBody ShelfRequestDTO shelfRequestDTO) {
        try {
            ShelfResponseDTO updated = shelfService.updateShelf(id, shelfRequestDTO);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete shelf
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShelf(@PathVariable Long id) {
        try {
            shelfService.deleteShelf(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add a new level to a shelf
     */
    @PostMapping("/{shelfId}/levels")
    @ResponseStatus(HttpStatus.CREATED)
    public ShelfLevelResponseDTO addLevel(@PathVariable Long shelfId,
                                          @RequestParam Integer levelPosition) {
        return shelfService.addLevel(shelfId, levelPosition);
    }

    /**
     * Remove a level from a shelf
     */
    @DeleteMapping("/{shelfId}/levels/{levelPosition}")
    public ResponseEntity<Void> removeLevel(@PathVariable Long shelfId,
                                            @PathVariable Integer levelPosition) {
        try {
            shelfService.removeLevel(shelfId, levelPosition);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Exception handler for validation and business logic errors
     */
    @ExceptionHandler({IllegalArgumentException.class, NoSuchElementException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleError(Exception ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(IllegalArgumentException ex) {
        return ex.getMessage();
    }
}
