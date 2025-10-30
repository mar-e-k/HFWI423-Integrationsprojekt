package de.fhdw.kassensystem.persistence.repository;

import de.fhdw.kassensystem.persistence.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article,Long> {

    // Diese Methode wird von Spring Data JPA automatisch implementiert.
    // Sie sucht nach allen Artikeln, deren 'name'-Feld den übergebenen
    List<Article> findByNameContainingIgnoreCase(String searchTerm);

}
