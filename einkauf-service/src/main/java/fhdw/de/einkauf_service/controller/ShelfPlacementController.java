package fhdw.de.einkauf_service.controller;

import fhdw.de.einkauf_service.dto.ShelfPlacementRequestDTO;
import fhdw.de.einkauf_service.dto.ShelfPlacementResponseDTO;
import fhdw.de.einkauf_service.exception.OverlapException;
import fhdw.de.einkauf_service.exception.OutOfBoundsException;
import fhdw.de.einkauf_service.service.ShelfPlacementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/shelf-placements")
public class ShelfPlacementController {

    private final ShelfPlacementService placementService;

    public ShelfPlacementController(ShelfPlacementService placementService) {
        this.placementService = placementService;
    }

    /**
     * Create a new article placement on a shelf level
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShelfPlacementResponseDTO createPlacement(@Valid @RequestBody ShelfPlacementRequestDTO placementRequestDTO) {
        return placementService.createPlacement(placementRequestDTO);
    }

    /**
     * Get placement by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShelfPlacementResponseDTO> getPlacementById(@PathVariable Long id) {
        try {
            ShelfPlacementResponseDTO placement = placementService.getPlacement(id);
            return ResponseEntity.ok(placement);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all placements on a specific shelf level
     */
    @GetMapping("/shelf-level/{shelfLevelId}")
    public ResponseEntity<List<ShelfPlacementResponseDTO>> getPlacementsByShelfLevel(@PathVariable Long shelfLevelId) {
        try {
            List<ShelfPlacementResponseDTO> placements = placementService.getPlacementsByShelfLevel(shelfLevelId);
            return ResponseEntity.ok(placements);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all placements of a specific article
     */
    @GetMapping("/article/{articleId}")
    public ResponseEntity<List<ShelfPlacementResponseDTO>> getPlacementsByArticle(@PathVariable Long articleId) {
        try {
            List<ShelfPlacementResponseDTO> placements = placementService.getPlacementsByArticle(articleId);
            return ResponseEntity.ok(placements);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update placement position and dimensions
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShelfPlacementResponseDTO> updatePlacement(@PathVariable Long id,
                                                                      @Valid @RequestBody ShelfPlacementRequestDTO placementRequestDTO) {
        try {
            ShelfPlacementResponseDTO updated = placementService.updatePlacement(id, placementRequestDTO);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete placement
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlacement(@PathVariable Long id) {
        try {
            placementService.deletePlacement(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Exception handlers for custom exceptions
     */
    @ExceptionHandler(OverlapException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleOverlapException(OverlapException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(OutOfBoundsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleOutOfBoundsException(OutOfBoundsException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex) {
        return ex.getMessage();
    }
}
