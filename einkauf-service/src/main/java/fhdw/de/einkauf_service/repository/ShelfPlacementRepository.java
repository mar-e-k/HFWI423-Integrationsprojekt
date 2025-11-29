package fhdw.de.einkauf_service.repository;

import fhdw.de.einkauf_service.entity.ShelfPlacement;
import fhdw.de.einkauf_service.entity.ShelfLevel;
import fhdw.de.einkauf_service.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelfPlacementRepository extends JpaRepository<ShelfPlacement, Long> {

    /**
     * Find all placements on a specific shelf level
     */
    List<ShelfPlacement> findByShelfLevel(ShelfLevel shelfLevel);

    /**
     * Find all placements of a specific article
     */
    List<ShelfPlacement> findByArticle(Article article);

    /**
     * Find a specific placement by shelf level and article
     */
    ShelfPlacement findByShelfLevelAndArticle(ShelfLevel shelfLevel, Article article);
}
