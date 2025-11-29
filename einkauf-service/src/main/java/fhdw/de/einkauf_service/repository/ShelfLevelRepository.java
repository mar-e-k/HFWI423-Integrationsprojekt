package fhdw.de.einkauf_service.repository;

import fhdw.de.einkauf_service.entity.ShelfLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShelfLevelRepository extends JpaRepository<ShelfLevel, Long> {

    Optional<ShelfLevel> findByShelfIdAndLevelPosition(Long shelfId, Integer levelPosition);

    List<ShelfLevel> findByShelfIdOrderByLevelPositionDesc(Long shelfId);
}
