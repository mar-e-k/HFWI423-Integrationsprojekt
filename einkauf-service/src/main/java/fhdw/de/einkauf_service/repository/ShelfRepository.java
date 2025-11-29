package fhdw.de.einkauf_service.repository;

import fhdw.de.einkauf_service.entity.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelfRepository extends JpaRepository<Shelf, Long> {

    List<Shelf> findByCategoryId(Long categoryId);
}
