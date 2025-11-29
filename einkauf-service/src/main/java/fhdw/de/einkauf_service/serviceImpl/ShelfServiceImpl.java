package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.dto.ShelfRequestDTO;
import fhdw.de.einkauf_service.dto.ShelfResponseDTO;
import fhdw.de.einkauf_service.dto.ShelfLevelResponseDTO;
import fhdw.de.einkauf_service.entity.Shelf;
import fhdw.de.einkauf_service.entity.ShelfLevel;
import fhdw.de.einkauf_service.entity.Category;
import fhdw.de.einkauf_service.repository.ShelfRepository;
import fhdw.de.einkauf_service.repository.ShelfLevelRepository;
import jakarta.persistence.EntityManager;
import fhdw.de.einkauf_service.service.ShelfService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ShelfServiceImpl implements ShelfService {

    private final ShelfRepository shelfRepository;
    private final ShelfLevelRepository shelfLevelRepository;
    private final EntityManager entityManager;

    public ShelfServiceImpl(ShelfRepository shelfRepository,
                          ShelfLevelRepository shelfLevelRepository,
                          EntityManager entityManager) {
        this.shelfRepository = shelfRepository;
        this.shelfLevelRepository = shelfLevelRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    @Override
    public ShelfResponseDTO createShelf(ShelfRequestDTO request) {
        // Load and validate category
        Category category = entityManager.find(Category.class, request.getCategoryId());
        if (category == null) {
            throw new NoSuchElementException("Category with ID " + request.getCategoryId() + " not found.");
        }

        // Create shelf entity
        Shelf shelf = new Shelf();
        shelf.setName(request.getName());
        shelf.setDescription(request.getDescription());
        shelf.setCategory(category);

        // Save shelf
        Shelf savedShelf = shelfRepository.save(shelf);

        // Create default level 1
        ShelfLevel level1 = new ShelfLevel();
        level1.setLevelPosition(1);
        level1.setShelf(savedShelf);
        shelfLevelRepository.save(level1);

        // Add level to shelf's levels set
        savedShelf.getLevels().add(level1);

        return toResponseDTO(savedShelf);
    }

    @Transactional(readOnly = true)
    @Override
    public ShelfResponseDTO getShelf(Long id) {
        Shelf shelf = shelfRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Shelf with ID " + id + " not found."));
        return toResponseDTO(shelf);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ShelfResponseDTO> getAllShelves() {
        return shelfRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ShelfResponseDTO> getShelfsByCategory(Long categoryId) {
        return shelfRepository.findByCategoryId(categoryId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ShelfResponseDTO updateShelf(Long id, ShelfRequestDTO request) {
        Shelf shelf = shelfRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Shelf with ID " + id + " not found."));

        // Category is immutable - validate that categoryId hasn't changed
        if (!shelf.getCategory().getId().equals(request.getCategoryId())) {
            throw new IllegalArgumentException("Kategorie eines Regals kann nicht geändert werden.");
        }

        // Update only name and description
        shelf.setName(request.getName());
        shelf.setDescription(request.getDescription());

        Shelf updatedShelf = shelfRepository.save(shelf);
        return toResponseDTO(updatedShelf);
    }

    @Transactional
    @Override
    public void deleteShelf(Long id) {
        if (!shelfRepository.existsById(id)) {
            throw new NoSuchElementException("Shelf with ID " + id + " not found.");
        }
        shelfRepository.deleteById(id);
    }

    @Transactional
    @Override
    public ShelfLevelResponseDTO addLevel(Long shelfId, Integer levelPosition) {
        // Validate level position
        if (levelPosition == null || levelPosition < 1 || levelPosition > 5) {
            throw new IllegalArgumentException("Level-Position muss zwischen 1 und 5 liegen.");
        }

        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new NoSuchElementException("Shelf with ID " + shelfId + " not found."));

        // Check max levels
        if (shelf.getLevels().size() >= 5) {
            throw new IllegalArgumentException("Ein Regal kann maximal 5 Level haben.");
        }

        // Check if level already exists
        if (shelfLevelRepository.findByShelfIdAndLevelPosition(shelfId, levelPosition).isPresent()) {
            throw new IllegalArgumentException("Level mit Position " + levelPosition + " existiert bereits für dieses Regal.");
        }

        // Create new level
        ShelfLevel newLevel = new ShelfLevel();
        newLevel.setLevelPosition(levelPosition);
        newLevel.setShelf(shelf);

        ShelfLevel savedLevel = shelfLevelRepository.save(newLevel);
        shelf.getLevels().add(savedLevel);

        return toLevelResponseDTO(savedLevel);
    }

    @Transactional
    @Override
    public void removeLevel(Long shelfId, Integer levelPosition) {
        ShelfLevel level = shelfLevelRepository.findByShelfIdAndLevelPosition(shelfId, levelPosition)
                .orElseThrow(() -> new NoSuchElementException(
                        "Level mit Position " + levelPosition + " für Regal " + shelfId + " nicht gefunden."));

        shelfLevelRepository.delete(level);
    }

    // ==================================================================================
    // PRIVATE MAPPING METHODS
    // ==================================================================================

    private ShelfResponseDTO toResponseDTO(Shelf entity) {
        ShelfResponseDTO dto = new ShelfResponseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setWidthCm(entity.getWidthCm());
        dto.setHeightCm(entity.getHeightCm());
        dto.setDepthCm(entity.getDepthCm());

        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
            dto.setCategoryName(entity.getCategory().getName());
        }

        if (entity.getLevels() != null) {
            dto.setLevels(entity.getLevels().stream()
                    .map(this::toLevelResponseDTO)
                    .collect(Collectors.toList()));
        }

        dto.setDateCreated(entity.getDateCreated());
        dto.setDateUpdated(entity.getDateUpdated());

        return dto;
    }

    private ShelfLevelResponseDTO toLevelResponseDTO(ShelfLevel entity) {
        ShelfLevelResponseDTO dto = new ShelfLevelResponseDTO();
        dto.setId(entity.getId());
        dto.setLevelPosition(entity.getLevelPosition());
        dto.setShelfId(entity.getShelf() != null ? entity.getShelf().getId() : null);
        // TODO: Get placement count from database when HI-53 is implemented
        dto.setPlacementCount(0);
        dto.setDateCreated(entity.getDateCreated());

        return dto;
    }
}
