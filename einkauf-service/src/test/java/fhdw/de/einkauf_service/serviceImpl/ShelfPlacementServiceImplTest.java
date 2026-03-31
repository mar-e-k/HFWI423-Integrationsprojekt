package fhdw.de.einkauf_service.serviceImpl;

import fhdw.de.einkauf_service.dto.ShelfPlacementRequestDTO;
import fhdw.de.einkauf_service.dto.ShelfPlacementResponseDTO;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.entity.ShelfLevel;
import fhdw.de.einkauf_service.entity.ShelfPlacement;
import fhdw.de.einkauf_service.exception.OutOfBoundsException;
import fhdw.de.einkauf_service.exception.OverlapException;
import fhdw.de.einkauf_service.repository.ShelfLevelRepository;
import fhdw.de.einkauf_service.repository.ShelfPlacementRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShelfPlacementServiceImplTest {

    @Mock
    private ShelfPlacementRepository placementRepository;

    @Mock
    private ShelfLevelRepository shelfLevelRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ShelfPlacementServiceImpl service;

    private ShelfPlacementRequestDTO buildRequest(double x, double y, double w, double h) {
        return new ShelfPlacementRequestDTO(1L, 1L, x, y, w, h);
    }

    private ShelfLevel buildLevel(Long id) {
        ShelfLevel level = new ShelfLevel();
        level.setId(id);
        level.setLevelPosition(1);
        return level;
    }

    private Article buildArticle(Long id) {
        Article a = new Article();
        a.setId(id);
        a.setArticleNumber("12345678");
        a.setName("Testartikel");
        return a;
    }

    private ShelfPlacement buildExistingPlacement(Long id, double x, double y, double w, double h) {
        ShelfPlacement p = new ShelfPlacement();
        p.setId(id);
        p.setPositionX(x);
        p.setPositionY(y);
        p.setWidthCm(w);
        p.setHeightCm(h);
        p.setArticle(buildArticle(1L));
        return p;
    }

    private void stubLevelAndArticle() {
        when(shelfLevelRepository.findById(1L)).thenReturn(Optional.of(buildLevel(1L)));
        when(entityManager.find(Article.class, 1L)).thenReturn(buildArticle(1L));
    }

    // ======================================
    // createPlacement — bounds validation
    // ======================================

    @Test
    void createPlacement_happyPath_withinBounds_noOverlap_savesAndReturnsDTO() {
        stubLevelAndArticle();
        when(placementRepository.findByShelfLevel(any())).thenReturn(List.of());
        ShelfPlacement saved = buildExistingPlacement(1L, 0, 0, 20, 20);
        when(placementRepository.save(any())).thenReturn(saved);

        ShelfPlacementResponseDTO result = service.createPlacement(buildRequest(0, 0, 20, 20));

        assertThat(result).isNotNull();
        verify(placementRepository).save(any(ShelfPlacement.class));
    }

    @Test
    void createPlacement_negativeX_throwsOutOfBoundsException() {
        stubLevelAndArticle();

        assertThatThrownBy(() -> service.createPlacement(buildRequest(-1, 0, 10, 10)))
                .isInstanceOf(OutOfBoundsException.class);
    }

    @Test
    void createPlacement_xPlusWidthExceedsShelfWidth_throwsOutOfBoundsException() {
        stubLevelAndArticle();

        // x=90 + w=20 = 110 > 100
        assertThatThrownBy(() -> service.createPlacement(buildRequest(90, 0, 20, 10)))
                .isInstanceOf(OutOfBoundsException.class);
    }

    @Test
    void createPlacement_xPlusWidthExactlyAtBoundary_doesNotThrow() {
        stubLevelAndArticle();
        when(placementRepository.findByShelfLevel(any())).thenReturn(List.of());
        ShelfPlacement saved = buildExistingPlacement(1L, 80, 0, 20, 20);
        when(placementRepository.save(any())).thenReturn(saved);

        // x=80 + w=20 = 100 exactly — should pass
        assertThatCode(() -> service.createPlacement(buildRequest(80, 0, 20, 20)))
                .doesNotThrowAnyException();
    }

    @Test
    void createPlacement_negativeY_throwsOutOfBoundsException() {
        stubLevelAndArticle();

        assertThatThrownBy(() -> service.createPlacement(buildRequest(0, -1, 10, 10)))
                .isInstanceOf(OutOfBoundsException.class);
    }

    @Test
    void createPlacement_yPlusHeightExceedsShelfHeight_throwsOutOfBoundsException() {
        stubLevelAndArticle();

        // y=140 + h=20 = 160 > 150
        assertThatThrownBy(() -> service.createPlacement(buildRequest(0, 140, 10, 20)))
                .isInstanceOf(OutOfBoundsException.class);
    }

    @Test
    void createPlacement_yPlusHeightExactlyAtBoundary_doesNotThrow() {
        stubLevelAndArticle();
        when(placementRepository.findByShelfLevel(any())).thenReturn(List.of());
        ShelfPlacement saved = buildExistingPlacement(1L, 0, 130, 10, 20);
        when(placementRepository.save(any())).thenReturn(saved);

        // y=130 + h=20 = 150 exactly — should pass
        assertThatCode(() -> service.createPlacement(buildRequest(0, 130, 10, 20)))
                .doesNotThrowAnyException();
    }

    @Test
    void createPlacement_shelfLevelNotFound_throwsNoSuchElementException() {
        when(shelfLevelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createPlacement(buildRequest(0, 0, 10, 10)))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void createPlacement_articleNotFound_throwsNoSuchElementException() {
        when(shelfLevelRepository.findById(1L)).thenReturn(Optional.of(buildLevel(1L)));
        when(entityManager.find(Article.class, 1L)).thenReturn(null);

        assertThatThrownBy(() -> service.createPlacement(buildRequest(0, 0, 10, 10)))
                .isInstanceOf(NoSuchElementException.class);
    }

    // ======================================
    // createPlacement — overlap detection
    // ======================================

    @Test
    void createPlacement_overlappingExistingPlacement_throwsOverlapException() {
        stubLevelAndArticle();
        // Existing: (15,15,20,20) overlaps with request (10,10,20,20)
        when(placementRepository.findByShelfLevel(any())).thenReturn(
                List.of(buildExistingPlacement(1L, 15, 15, 20, 20))
        );

        assertThatThrownBy(() -> service.createPlacement(buildRequest(10, 10, 20, 20)))
                .isInstanceOf(OverlapException.class);
    }

    @Test
    void createPlacement_adjacentOnXAxis_doesNotThrow() {
        stubLevelAndArticle();
        // Request (0,0,10,10), existing (10,0,10,10) — touching but not overlapping
        when(placementRepository.findByShelfLevel(any())).thenReturn(
                List.of(buildExistingPlacement(1L, 10, 0, 10, 10))
        );
        ShelfPlacement saved = buildExistingPlacement(2L, 0, 0, 10, 10);
        when(placementRepository.save(any())).thenReturn(saved);

        assertThatCode(() -> service.createPlacement(buildRequest(0, 0, 10, 10)))
                .doesNotThrowAnyException();
    }

    @Test
    void createPlacement_adjacentOnYAxis_doesNotThrow() {
        stubLevelAndArticle();
        // Request (0,0,10,10), existing (0,10,10,10) — touching vertically
        when(placementRepository.findByShelfLevel(any())).thenReturn(
                List.of(buildExistingPlacement(1L, 0, 10, 10, 10))
        );
        ShelfPlacement saved = buildExistingPlacement(2L, 0, 0, 10, 10);
        when(placementRepository.save(any())).thenReturn(saved);

        assertThatCode(() -> service.createPlacement(buildRequest(0, 0, 10, 10)))
                .doesNotThrowAnyException();
    }

    @Test
    void createPlacement_completelyLeftOfExisting_doesNotThrow() {
        stubLevelAndArticle();
        when(placementRepository.findByShelfLevel(any())).thenReturn(
                List.of(buildExistingPlacement(1L, 20, 0, 10, 10))
        );
        ShelfPlacement saved = buildExistingPlacement(2L, 0, 0, 5, 5);
        when(placementRepository.save(any())).thenReturn(saved);

        assertThatCode(() -> service.createPlacement(buildRequest(0, 0, 5, 5)))
                .doesNotThrowAnyException();
    }

    @Test
    void createPlacement_completelyAboveExisting_doesNotThrow() {
        stubLevelAndArticle();
        when(placementRepository.findByShelfLevel(any())).thenReturn(
                List.of(buildExistingPlacement(1L, 0, 0, 10, 10))
        );
        ShelfPlacement saved = buildExistingPlacement(2L, 0, 15, 5, 5);
        when(placementRepository.save(any())).thenReturn(saved);

        assertThatCode(() -> service.createPlacement(buildRequest(0, 15, 5, 5)))
                .doesNotThrowAnyException();
    }

    @Test
    void createPlacement_partialXOverlapFullYOverlap_throwsOverlapException() {
        stubLevelAndArticle();
        // Request (5,0,10,5), existing (10,0,5,5): X overlaps (5<15 && 15>10), Y overlaps (0<5 && 5>0)
        when(placementRepository.findByShelfLevel(any())).thenReturn(
                List.of(buildExistingPlacement(1L, 10, 0, 5, 5))
        );

        assertThatThrownBy(() -> service.createPlacement(buildRequest(5, 0, 10, 5)))
                .isInstanceOf(OverlapException.class);
    }

    @Test
    void createPlacement_multipleExistingNoOverlap_doesNotThrow() {
        stubLevelAndArticle();
        // Two placements at (0,0,20,20) and (40,0,20,20), request at (25,0,10,10) — fits in gap
        when(placementRepository.findByShelfLevel(any())).thenReturn(List.of(
                buildExistingPlacement(1L, 0, 0, 20, 20),
                buildExistingPlacement(2L, 40, 0, 20, 20)
        ));
        ShelfPlacement saved = buildExistingPlacement(3L, 25, 0, 10, 10);
        when(placementRepository.save(any())).thenReturn(saved);

        assertThatCode(() -> service.createPlacement(buildRequest(25, 0, 10, 10)))
                .doesNotThrowAnyException();
    }

    // ======================================
    // updatePlacement
    // ======================================

    @Test
    void updatePlacement_notFound_throwsNoSuchElementException() {
        when(placementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePlacement(99L, buildRequest(0, 0, 10, 10)))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void updatePlacement_excludesCurrentPlacementFromOverlapCheck() {
        ShelfLevel level = buildLevel(1L);
        ShelfPlacement existing = buildExistingPlacement(1L, 0, 0, 20, 20);
        existing.setShelfLevel(level);

        when(placementRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(shelfLevelRepository.findById(1L)).thenReturn(Optional.of(level));
        // Only placement in the list is the one being updated — should be excluded
        when(placementRepository.findByShelfLevel(level)).thenReturn(List.of(existing));
        when(placementRepository.save(any())).thenReturn(existing);

        assertThatCode(() -> service.updatePlacement(1L, buildRequest(0, 0, 20, 20)))
                .doesNotThrowAnyException();
    }

    // ======================================
    // calculateNextAvailablePosition
    // ======================================

    @Test
    void calculateNextAvailablePosition_emptyLevel_returnsZero() {
        ShelfLevel level = buildLevel(1L);
        when(shelfLevelRepository.findById(1L)).thenReturn(Optional.of(level));
        when(placementRepository.findByShelfLevel(level)).thenReturn(List.of());

        Double result = service.calculateNextAvailablePosition(1L, 20.0);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void calculateNextAvailablePosition_oneExistingAtOrigin_returnsAfterIt() {
        ShelfLevel level = buildLevel(1L);
        when(shelfLevelRepository.findById(1L)).thenReturn(Optional.of(level));
        when(placementRepository.findByShelfLevel(level)).thenReturn(
                List.of(buildExistingPlacement(1L, 0, 0, 30, 20))
        );

        Double result = service.calculateNextAvailablePosition(1L, 20.0);

        assertThat(result).isEqualTo(30.0);
    }

    @Test
    void calculateNextAvailablePosition_gapBeforeFirstFitsItem_returnsZero() {
        ShelfLevel level = buildLevel(1L);
        when(shelfLevelRepository.findById(1L)).thenReturn(Optional.of(level));
        // Existing at x=40, gap 0-40 is 40 wide, item needs 30
        when(placementRepository.findByShelfLevel(level)).thenReturn(
                List.of(buildExistingPlacement(1L, 40, 0, 30, 20))
        );

        Double result = service.calculateNextAvailablePosition(1L, 30.0);

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void calculateNextAvailablePosition_noRoomLeft_throwsOutOfBoundsException() {
        ShelfLevel level = buildLevel(1L);
        when(shelfLevelRepository.findById(1L)).thenReturn(Optional.of(level));
        // Existing fills x=0 to x=90, item needs 20 → 90+20=110 > 100
        when(placementRepository.findByShelfLevel(level)).thenReturn(
                List.of(buildExistingPlacement(1L, 0, 0, 90, 20))
        );

        assertThatThrownBy(() -> service.calculateNextAvailablePosition(1L, 20.0))
                .isInstanceOf(OutOfBoundsException.class);
    }

    @Test
    void calculateNextAvailablePosition_levelNotFound_throwsNoSuchElementException() {
        when(shelfLevelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calculateNextAvailablePosition(99L, 10.0))
                .isInstanceOf(NoSuchElementException.class);
    }

    // ======================================
    // Read operations
    // ======================================

    @Test
    void getPlacement_found_returnsDTO() {
        ShelfPlacement placement = buildExistingPlacement(1L, 0, 0, 10, 10);
        placement.setShelfLevel(buildLevel(1L));
        when(placementRepository.findById(1L)).thenReturn(Optional.of(placement));

        ShelfPlacementResponseDTO result = service.getPlacement(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getPlacement_notFound_throwsNoSuchElementException() {
        when(placementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPlacement(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getPlacementsByShelfLevel_found_returnsList() {
        ShelfLevel level = buildLevel(1L);
        ShelfPlacement p = buildExistingPlacement(1L, 0, 0, 10, 10);
        p.setShelfLevel(level);

        when(shelfLevelRepository.findById(1L)).thenReturn(Optional.of(level));
        when(placementRepository.findByShelfLevel(level)).thenReturn(List.of(p));

        List<ShelfPlacementResponseDTO> result = service.getPlacementsByShelfLevel(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getPlacementsByShelfLevel_levelNotFound_throwsNoSuchElementException() {
        when(shelfLevelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPlacementsByShelfLevel(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getPlacementsByArticle_articleNotFound_throwsNoSuchElementException() {
        when(entityManager.find(Article.class, 99L)).thenReturn(null);

        ShelfPlacementRequestDTO req = new ShelfPlacementRequestDTO(1L, 99L, 0.0, 0.0, 10.0, 10.0);
        assertThatThrownBy(() -> service.getPlacementsByArticle(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void deletePlacement_exists_callsDeleteById() {
        when(placementRepository.existsById(1L)).thenReturn(true);

        service.deletePlacement(1L);

        verify(placementRepository).deleteById(1L);
    }

    @Test
    void deletePlacement_notFound_throwsNoSuchElementException() {
        when(placementRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.deletePlacement(99L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
