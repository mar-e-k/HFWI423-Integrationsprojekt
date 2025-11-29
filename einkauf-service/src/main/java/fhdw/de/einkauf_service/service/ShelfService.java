package fhdw.de.einkauf_service.service;

import fhdw.de.einkauf_service.dto.ShelfRequestDTO;
import fhdw.de.einkauf_service.dto.ShelfResponseDTO;
import fhdw.de.einkauf_service.dto.ShelfLevelResponseDTO;

import java.util.List;

public interface ShelfService {

    /**
     * Create a new shelf with initial level 1
     */
    ShelfResponseDTO createShelf(ShelfRequestDTO request);

    /**
     * Get shelf by ID
     */
    ShelfResponseDTO getShelf(Long id);

    /**
     * Get all shelves
     */
    List<ShelfResponseDTO> getAllShelves();

    /**
     * Get all shelves for a specific category
     */
    List<ShelfResponseDTO> getShelfsByCategory(Long categoryId);

    /**
     * Update shelf (name, description only; category is immutable)
     */
    ShelfResponseDTO updateShelf(Long id, ShelfRequestDTO request);

    /**
     * Delete shelf and all associated levels and placements
     */
    void deleteShelf(Long id);

    /**
     * Add a new level to a shelf (max 5 levels)
     */
    ShelfLevelResponseDTO addLevel(Long shelfId, Integer levelPosition);

    /**
     * Remove a level from a shelf (cascade deletes placements)
     */
    void removeLevel(Long shelfId, Integer levelPosition);
}
