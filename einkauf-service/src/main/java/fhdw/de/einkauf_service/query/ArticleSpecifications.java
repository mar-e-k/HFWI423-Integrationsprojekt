package fhdw.de.einkauf_service.query;

import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import fhdw.de.einkauf_service.entity.Article;
import fhdw.de.einkauf_service.entity.Category;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ArticleSpecifications {

    /**
     * Erzeugt eine Specification, um Artikel basierend auf den Kriterien im Filter-DTO zu filtern.
     */
    public static Specification<Article> filterArticles(ArticleFilterDTO filter) {

        // Die Lambda-Funktion definiert, wie die Query gebaut wird
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // 1. Suche nach NAME (LIKE %name%, ignoriert Groß-/Kleinschreibung)
            if (StringUtils.hasText(filter.getName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase() + "%"
                ));
            }

            // 2. Suche nach ARTIKELNUMMER (LIKE %number%, ignoriert Groß-/Kleinschreibung)
            if (StringUtils.hasText(filter.getArticleNumber())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("articleNumber")),
                        "%" + filter.getArticleNumber().toLowerCase() + "%"
                ));
            }

            // 3. Filtern nach KATEGORIE (Exakte Übereinstimmung)
            if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {

                Join<Article, Category> categoryJoin = root.join("categories", JoinType.INNER);
                Predicate categoryPredicate = categoryJoin.get("id").in(filter.getCategoryIds());
                predicates.add(categoryPredicate);
                query.distinct(true);
            }


            // 4. Filtern nach LIEFERANT / HERSTELLER (Exakte Übereinstimmung)
            if (filter.getSupplierId() != null) {
                // 1. Zugriff auf die Supplier-Entität des Artikels: root.get("supplier")
                // 2. Zugriff auf das ID-Feld des Suppliers: .get("id")
                predicates.add(criteriaBuilder.equal(
                        root.get("supplier").get("id"),
                        filter.getSupplierId()
                ));
            }

            // 5. Filtern nach HERSTELLER (Exakte Übereinstimmung)
            if (StringUtils.hasText(filter.getManufacturer())) {
                predicates.add(criteriaBuilder.equal(
                        root.get("manufacturer"), // Stellt sicher, dass dieses Feld in deiner Entity existiert
                        filter.getManufacturer()
                ));
            }

            // 6. Statusfilter: Filtern nach Verfügbarkeit
            // null = alle Artikel, true = nur verfügbar, false = nur nicht verfügbar
            if (filter.getIsAvailable() != null) {
                if (filter.getIsAvailable()) {
                    predicates.add(criteriaBuilder.isTrue(root.get("isAvailable")));
                } else {
                    predicates.add(criteriaBuilder.isFalse(root.get("isAvailable")));
                }
            }

            // Verknüpfe alle gesammelten Predicates mit logischem AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
