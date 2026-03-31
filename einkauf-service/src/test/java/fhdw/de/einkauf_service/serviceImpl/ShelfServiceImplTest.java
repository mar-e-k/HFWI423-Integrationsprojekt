package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.dto.ShelfLevelResponseDTO;
import fhdw.de.einkauf_service.dto.ShelfRequestDTO;
import fhdw.de.einkauf_service.dto.ShelfResponseDTO;
import fhdw.de.einkauf_service.entity.Category;
import fhdw.de.einkauf_service.entity.Shelf;
import fhdw.de.einkauf_service.entity.ShelfLevel;
import fhdw.de.einkauf_service.repository.ShelfLevelRepository;
import fhdw.de.einkauf_service.repository.ShelfRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShelfServiceImplTest {

    @Mock
    private ShelfRepository shelfRepository;

    @Mock
    private ShelfLevelRepository shelfLevelRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ShelfServiceImpl service;

    private Category buildCategory(Long id) {
        Category c = new Category();
        c.setId(id);
        c.setName("Testkategorie");
        return c;
    }

    private Shelf buildShelf(Long id, Category category, int levelCount) {
        Shelf s = new Shelf();
        s.setId(id);
        s.setName("Testregal");
        s.setDescription("Beschreibung");
        s.setCategory(category);
        Set<ShelfLevel> levels = new HashSet<>();
        for (int i = 1; i <= levelCount; i++) {
            ShelfLevel level = new ShelfLevel();
            level.setId((long) i);
            level.setLevelPosition(i);
            level.setShelf(s);
            levels.add(level);
        }
        s.setLevels(levels);
        return s;
    }

    private ShelfLevel buildLevel(Long id, int position, Shelf shelf) {
        ShelfLevel level = new ShelfLevel();
        level.setId(id);
        level.setLevelPosition(position);
        level.setShelf(shelf);
        return level;
    }

    // --- createShelf ---

    @Test
    void createShelf_categoryExists_savesShelfAndDefaultLevel() {
        Category category = buildCategory(1L);
        ShelfRequestDTO request = new ShelfRequestDTO("Regal A", "Beschreibung", 1L);

        Shelf savedShelf = buildShelf(1L, category, 0);
        ShelfLevel savedLevel = buildLevel(1L, 1, savedShelf);

        when(entityManager.find(Category.class, 1L)).thenReturn(category);
        when(shelfRepository.save(any(Shelf.class))).thenReturn(savedShelf);
        when(shelfLevelRepository.save(any(ShelfLevel.class))).thenReturn(savedLevel);

        ShelfResponseDTO result = service.createShelf(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(shelfLevelRepository, times(1)).save(any(ShelfLevel.class));
    }

    @Test
    void createShelf_categoryNotFound_throwsNoSuchElementException() {
        when(entityManager.find(Category.class, 99L)).thenReturn(null);

        assertThatThrownBy(() -> service.createShelf(new ShelfRequestDTO("X", "Y", 99L)))
                .isInstanceOf(NoSuchElementException.class);
    }

    // --- getShelf ---

    @Test
    void getShelf_found_returnsDTO() {
        Category category = buildCategory(1L);
        when(shelfRepository.findById(1L)).thenReturn(Optional.of(buildShelf(1L, category, 1)));

        ShelfResponseDTO result = service.getShelf(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getShelf_notFound_throwsNoSuchElementException() {
        when(shelfRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getShelf(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    // --- getAllShelves ---

    @Test
    void getAllShelves_returnsMappedList() {
        Category category = buildCategory(1L);
        when(shelfRepository.findAll()).thenReturn(List.of(
                buildShelf(1L, category, 1),
                buildShelf(2L, category, 2)
        ));

        List<ShelfResponseDTO> result = service.getAllShelves();

        assertThat(result).hasSize(2);
    }

    // --- getShelfsByCategory ---

    @Test
    void getShelfsByCategory_returnsMappedList() {
        Category category = buildCategory(1L);
        when(shelfRepository.findByCategoryId(1L)).thenReturn(List.of(buildShelf(1L, category, 1)));

        List<ShelfResponseDTO> result = service.getShelfsByCategory(1L);

        assertThat(result).hasSize(1);
    }

    // --- updateShelf ---

    @Test
    void updateShelf_happyPath_updatesNameAndDescription() {
        Category category = buildCategory(1L);
        Shelf existing = buildShelf(1L, category, 1);
        Shelf updated = buildShelf(1L, category, 1);
        updated.setName("Neuer Name");

        when(shelfRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(shelfRepository.save(any(Shelf.class))).thenReturn(updated);

        ShelfResponseDTO result = service.updateShelf(1L, new ShelfRequestDTO("Neuer Name", "desc", 1L));

        assertThat(result.getName()).isEqualTo("Neuer Name");
    }

    @Test
    void updateShelf_differentCategory_throwsIllegalArgumentException() {
        Category category = buildCategory(1L);
        Shelf existing = buildShelf(1L, category, 1);

        when(shelfRepository.findById(1L)).thenReturn(Optional.of(existing));

        // Request uses categoryId=2 but shelf has categoryId=1
        assertThatThrownBy(() -> service.updateShelf(1L, new ShelfRequestDTO("X", "Y", 2L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateShelf_notFound_throwsNoSuchElementException() {
        when(shelfRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateShelf(99L, new ShelfRequestDTO("X", "Y", 1L)))
                .isInstanceOf(NoSuchElementException.class);
    }

    // --- deleteShelf ---

    @Test
    void deleteShelf_exists_callsDeleteById() {
        when(shelfRepository.existsById(1L)).thenReturn(true);

        service.deleteShelf(1L);

        verify(shelfRepository).deleteById(1L);
    }

    @Test
    void deleteShelf_notFound_throwsNoSuchElementException() {
        when(shelfRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteShelf(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    // --- addLevel ---

    @Test
    void addLevel_validPosition_belowMax_savesAndReturnsDTO() {
        Category category = buildCategory(1L);
        Shelf shelf = buildShelf(1L, category, 2); // 2 existing levels
        ShelfLevel newLevel = buildLevel(3L, 3, shelf);

        when(shelfRepository.findById(1L)).thenReturn(Optional.of(shelf));
        when(shelfLevelRepository.findByShelfIdAndLevelPosition(1L, 3)).thenReturn(Optional.empty());
        when(shelfLevelRepository.save(any(ShelfLevel.class))).thenReturn(newLevel);

        ShelfLevelResponseDTO result = service.addLevel(1L, 3);

        assertThat(result.getLevelPosition()).isEqualTo(3);
        verify(shelfLevelRepository).save(any(ShelfLevel.class));
    }

    @Test
    void addLevel_positionNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.addLevel(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addLevel_positionZero_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.addLevel(1L, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addLevel_positionSix_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.addLevel(1L, 6))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addLevel_alreadyFiveLevels_throwsIllegalArgumentException() {
        Category category = buildCategory(1L);
        Shelf shelf = buildShelf(1L, category, 5); // already at max

        when(shelfRepository.findById(1L)).thenReturn(Optional.of(shelf));

        assertThatThrownBy(() -> service.addLevel(1L, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximal 5");
    }

    @Test
    void addLevel_positionAlreadyExists_throwsIllegalArgumentException() {
        Category category = buildCategory(1L);
        Shelf shelf = buildShelf(1L, category, 1);
        ShelfLevel existingLevel = buildLevel(1L, 2, shelf);

        when(shelfRepository.findById(1L)).thenReturn(Optional.of(shelf));
        when(shelfLevelRepository.findByShelfIdAndLevelPosition(1L, 2)).thenReturn(Optional.of(existingLevel));

        assertThatThrownBy(() -> service.addLevel(1L, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existiert bereits");
    }

    @Test
    void addLevel_shelfNotFound_throwsNoSuchElementException() {
        when(shelfRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addLevel(99L, 2))
                .isInstanceOf(NoSuchElementException.class);
    }

    // --- removeLevel ---

    @Test
    void removeLevel_found_callsDelete() {
        Category category = buildCategory(1L);
        Shelf shelf = buildShelf(1L, category, 1);
        ShelfLevel level = buildLevel(1L, 1, shelf);

        when(shelfLevelRepository.findByShelfIdAndLevelPosition(1L, 1)).thenReturn(Optional.of(level));

        service.removeLevel(1L, 1);

        verify(shelfLevelRepository).delete(level);
    }

    @Test
    void removeLevel_notFound_throwsNoSuchElementException() {
        when(shelfLevelRepository.findByShelfIdAndLevelPosition(1L, 3)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeLevel(1L, 3))
                .isInstanceOf(NoSuchElementException.class);
    }
}
