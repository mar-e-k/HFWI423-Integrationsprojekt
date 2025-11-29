package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.dto.ShelfPlacementRequestDTO;
import fhdw.de.einkauf_service.dto.ShelfPlacementResponseDTO;

import java.util.List;

public interface ShelfPlacementService {

    /**
     * Create a new placement on a shelf level with overlap detection
     * Throws OverlapException if placement overlaps with existing placements
     * Throws OutOfBoundsException if placement extends beyond shelf boundaries
     */
    ShelfPlacementResponseDTO createPlacement(ShelfPlacementRequestDTO request);

    /**
     * Get a placement by ID
     */
    ShelfPlacementResponseDTO getPlacement(Long id);

    /**
     * Get all placements on a specific shelf level
     */
    List<ShelfPlacementResponseDTO> getPlacementsByShelfLevel(Long shelfLevelId);

    /**
     * Get all placements of a specific article across all shelves
     */
    List<ShelfPlacementResponseDTO> getPlacementsByArticle(Long articleId);

    /**
     * Update a placement position and dimensions with overlap/bounds validation
     */
    ShelfPlacementResponseDTO updatePlacement(Long id, ShelfPlacementRequestDTO request);

    /**
     * Delete a placement from a shelf level
     */
    void deletePlacement(Long id);
}
