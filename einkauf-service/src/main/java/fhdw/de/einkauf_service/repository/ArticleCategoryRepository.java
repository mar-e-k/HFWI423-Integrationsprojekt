package fhdw.de.einkauf_service.repository;

import fhdw.de.einkauf_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleCategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

}
