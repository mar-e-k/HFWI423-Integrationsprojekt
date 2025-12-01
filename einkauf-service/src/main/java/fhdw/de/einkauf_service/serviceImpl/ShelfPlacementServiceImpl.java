package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.dto.ShelfPlacementRequestDTO;
import fhdw.de.einkauf_service.dto.ShelfPlacementResponseDTO;
import fhdw.de.einkauf_service.entity.ShelfPlacement;
import fhdw.de.einkauf_service.entity.ShelfLevel;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.exception.OverlapException;
import fhdw.de.einkauf_service.exception.OutOfBoundsException;
import fhdw.de.einkauf_service.repository.ShelfPlacementRepository;
import fhdw.de.einkauf_service.repository.ShelfLevelRepository;
import jakarta.persistence.EntityManager;
import fhdw.de.einkauf_service.service.ShelfPlacementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ShelfPlacementServiceImpl implements ShelfPlacementService {

    // Shelf fixed dimensions
    private static final Double SHELF_WIDTH = 100.0;    // cm
    private static final Double SHELF_HEIGHT = 150.0;   // cm

    private final ShelfPlacementRepository placementRepository;
    private final ShelfLevelRepository shelfLevelRepository;
    private final EntityManager entityManager;

    public ShelfPlacementServiceImpl(ShelfPlacementRepository placementRepository,
                                    ShelfLevelRepository shelfLevelRepository,
                                    EntityManager entityManager) {
        this.placementRepository = placementRepository;
        this.shelfLevelRepository = shelfLevelRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    @Override
    public ShelfPlacementResponseDTO createPlacement(ShelfPlacementRequestDTO request) {
        // Load shelf level and article
        ShelfLevel shelfLevel = shelfLevelRepository.findById(request.getShelfLevelId())
                .orElseThrow(() -> new NoSuchElementException(
                        "ShelfLevel with ID " + request.getShelfLevelId() + " not found."));

        Article article = entityManager.find(Article.class, request.getArticleId());
        if (article == null) {
            throw new NoSuchElementException("Article with ID " + request.getArticleId() + " not found.");
        }

        // Validate bounds
        validateBounds(request);

        // Check for overlaps with existing placements
        validateNoOverlap(shelfLevel, request, null);

        // Create and save placement
        ShelfPlacement placement = new ShelfPlacement();
        placement.setShelfLevel(shelfLevel);
        placement.setArticle(article);
        placement.setPositionX(request.getPositionX());
        placement.setPositionY(request.getPositionY());
        placement.setWidthCm(request.getWidthCm());
        placement.setHeightCm(request.getHeightCm());

        ShelfPlacement savedPlacement = placementRepository.save(placement);
        return toResponseDTO(savedPlacement);
    }

    @Transactional(readOnly = true)
    @Override
    public ShelfPlacementResponseDTO getPlacement(Long id) {
        ShelfPlacement placement = placementRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Placement with ID " + id + " not found."));
        return toResponseDTO(placement);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ShelfPlacementResponseDTO> getPlacementsByShelfLevel(Long shelfLevelId) {
        ShelfLevel shelfLevel = shelfLevelRepository.findById(shelfLevelId)
                .orElseThrow(() -> new NoSuchElementException("ShelfLevel with ID " + shelfLevelId + " not found."));

        return placementRepository.findByShelfLevel(shelfLevel).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ShelfPlacementResponseDTO> getPlacementsByArticle(Long articleId) {
        Article article = entityManager.find(Article.class, articleId);
        if (article == null) {
            throw new NoSuchElementException("Article with ID " + articleId + " not found.");
        }

        return placementRepository.findByArticle(article).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ShelfPlacementResponseDTO updatePlacement(Long id, ShelfPlacementRequestDTO request) {
        ShelfPlacement placement = placementRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Placement with ID " + id + " not found."));

        // Load shelf level (might be different from current level)
        ShelfLevel shelfLevel = shelfLevelRepository.findById(request.getShelfLevelId())
                .orElseThrow(() -> new NoSuchElementException(
                        "ShelfLevel with ID " + request.getShelfLevelId() + " not found."));

        // Validate bounds
        validateBounds(request);

        // Check for overlaps, excluding current placement
        validateNoOverlap(shelfLevel, request, placement.getId());

        // Update placement
        placement.setShelfLevel(shelfLevel);
        placement.setPositionX(request.getPositionX());
        placement.setPositionY(request.getPositionY());
        placement.setWidthCm(request.getWidthCm());
        placement.setHeightCm(request.getHeightCm());

        ShelfPlacement updatedPlacement = placementRepository.save(placement);
        return toResponseDTO(updatedPlacement);
    }

    @Transactional
    @Override
    public void deletePlacement(Long id) {
        if (!placementRepository.existsById(id)) {
            throw new NoSuchElementException("Placement with ID " + id + " not found.");
        }
        placementRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Double calculateNextAvailablePosition(Long shelfLevelId, Double widthCm) {
        ShelfLevel shelfLevel = shelfLevelRepository.findById(shelfLevelId)
                .orElseThrow(() -> new NoSuchElementException(
                        "ShelfLevel with ID " + shelfLevelId + " not found."));

        // Get all placements on this level sorted by X position
        List<ShelfPlacement> placements = placementRepository.findByShelfLevel(shelfLevel).stream()
                .sorted((p1, p2) -> Double.compare(p1.getPositionX(), p2.getPositionX()))
                .collect(Collectors.toList());

        // Start position at 0
        double currentX = 0.0;

        // Find next available position by checking each existing placement
        for (ShelfPlacement placement : placements) {
            // If the article fits before this placement, use current position
            if (currentX + widthCm <= placement.getPositionX()) {
                if (currentX + widthCm <= SHELF_WIDTH) {
                    return currentX;
                }
            }
            // Otherwise, move to the right of this placement
            currentX = Math.max(currentX, placement.getPositionX() + placement.getWidthCm());
        }

        // Check if there's room at the end
        if (currentX + widthCm > SHELF_WIDTH) {
            throw new OutOfBoundsException(
                    "Der Artikel passt nicht auf das Regal. Verfügbare Breite: " +
                    (SHELF_WIDTH - currentX) + " cm, benötigte Breite: " + widthCm + " cm");
        }

        return currentX;
    }

    // ==================================================================================
    // PRIVATE VALIDATION & HELPER METHODS
    // ==================================================================================

    /**
     * Validate that placement is within shelf bounds
     */
    private void validateBounds(ShelfPlacementRequestDTO request) {
        double maxX = SHELF_WIDTH;
        double maxY = SHELF_HEIGHT;

        if (request.getPositionX() < 0 || request.getPositionX() + request.getWidthCm() > maxX) {
            throw new OutOfBoundsException(
                    "Platzierung überschreitet horizontale Regalgrenzen (0-" + maxX + "cm). " +
                    "Position X: " + request.getPositionX() + ", Breite: " + request.getWidthCm());
        }

        if (request.getPositionY() < 0 || request.getPositionY() + request.getHeightCm() > maxY) {
            throw new OutOfBoundsException(
                    "Platzierung überschreitet vertikale Regalgrenzen (0-" + maxY + "cm). " +
                    "Position Y: " + request.getPositionY() + ", Höhe: " + request.getHeightCm());
        }
    }

    /**
     * Check if placement overlaps with any existing placements on the shelf level
     * @param shelfLevel The shelf level to check placements on
     * @param request The placement request to validate
     * @param excludePlacementId Optional: ID of placement to exclude from overlap check (for updates)
     */
    private void validateNoOverlap(ShelfLevel shelfLevel, ShelfPlacementRequestDTO request, Long excludePlacementId) {
        List<ShelfPlacement> existingPlacements = placementRepository.findByShelfLevel(shelfLevel);

        for (ShelfPlacement existing : existingPlacements) {
            // Skip the placement being updated
            if (excludePlacementId != null && existing.getId().equals(excludePlacementId)) {
                continue;
            }

            if (isOverlapping(request, existing)) {
                throw new OverlapException(
                        "Die Platzierung überlappt mit dem Artikel '" + existing.getArticle().getArticleNumber() + "' " +
                        "auf dem gleichen Level. Verschieben Sie die Platzierung.");
            }
        }
    }

    /**
     * Check if two rectangles overlap using AABB (Axis-Aligned Bounding Box) collision detection
     * Rectangle 1 (placement request) vs Rectangle 2 (existing placement)
     */
    private boolean isOverlapping(ShelfPlacementRequestDTO placement, ShelfPlacement existing) {
        // Two rectangles overlap if:
        // rect1.left < rect2.right AND rect1.right > rect2.left AND
        // rect1.bottom < rect2.top AND rect1.top > rect2.bottom

        return placement.getPositionX() < existing.getPositionX() + existing.getWidthCm() &&
               placement.getPositionX() + placement.getWidthCm() > existing.getPositionX() &&
               placement.getPositionY() < existing.getPositionY() + existing.getHeightCm() &&
               placement.getPositionY() + placement.getHeightCm() > existing.getPositionY();
    }

    // ==================================================================================
    // PRIVATE MAPPING METHODS
    // ==================================================================================

    private ShelfPlacementResponseDTO toResponseDTO(ShelfPlacement entity) {
        ShelfPlacementResponseDTO dto = new ShelfPlacementResponseDTO();
        dto.setId(entity.getId());
        dto.setShelfLevelId(entity.getShelfLevel() != null ? entity.getShelfLevel().getId() : null);
        dto.setArticleId(entity.getArticle() != null ? entity.getArticle().getId() : null);
        dto.setArticleName(entity.getArticle() != null ? entity.getArticle().getArticleNumber() : null);
        dto.setPositionX(entity.getPositionX());
        dto.setPositionY(entity.getPositionY());
        dto.setWidthCm(entity.getWidthCm());
        dto.setHeightCm(entity.getHeightCm());
        dto.setDateCreated(entity.getDateCreated());

        return dto;
    }
}
