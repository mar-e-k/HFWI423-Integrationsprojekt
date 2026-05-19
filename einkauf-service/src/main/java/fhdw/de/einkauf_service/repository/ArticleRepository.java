package fhdw.de.einkauf_service.repository;

import fhdw.de.einkauf_service.entity.Article;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    @EntityGraph(attributePaths = {
        "suppliers",
        "suppliers.contactPeople",
        "suppliers.paymentTerm",
        "mainSupplier",
        "mainSupplier.contactPeople",
        "mainSupplier.paymentTerm",
        "categories"
    })
    List<Article> findAll(Specification<Article> spec);

    @EntityGraph(attributePaths = {
        "suppliers",
        "suppliers.contactPeople",
        "suppliers.paymentTerm",
        "mainSupplier",
        "mainSupplier.contactPeople",
        "mainSupplier.paymentTerm",
        "categories"
    })
    Optional<Article> findById(Long id);

    @Query("SELECT a.articleNumber FROM Article a WHERE a.id = :id")
    Optional<String> findArticleNumberById(@Param("id") Long id);

    @Query("SELECT a.name FROM Article a WHERE a.id = :id")
    Optional<String> findNameById(@Param("id") Long id);

    /**
     * Finds an Article by its unique article number (GTIN).
     * Used for duplicate check.
     */
    Optional<Article> findByArticleNumber(String articleNumber);
}