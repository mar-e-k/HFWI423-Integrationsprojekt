package fhdw.de.einkauf_service.query;

import fhdw.de.einkauf_service.dto.ArticleFilterDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import fhdw.de.einkauf_service.entity.Article;
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
            if (StringUtils.hasText(filter.getCategory())) {
                predicates.add(criteriaBuilder.equal(
                        root.get("category"),
                        filter.getCategory()
                ));
            }

            // 4. Filtern nach LIEFERANT / HERSTELLER (Exakte Übereinstimmung)
            if (StringUtils.hasText(filter.getSupplier())) {
                predicates.add(criteriaBuilder.equal(
                        root.get("supplier"),
                        filter.getSupplier()
                ));
            }

            // 5. Filtern nach HERSTELLER (Exakte Übereinstimmung)
            if (StringUtils.hasText(filter.getManufacturer())) {
                predicates.add(criteriaBuilder.equal(
                        root.get("manufacturer"), // Stellt sicher, dass dieses Feld in deiner Entity existiert
                        filter.getManufacturer()
                ));
            }

            // 6. Statusfilter: Nur aktive/verfügbare Artikel (isAvailable = true)
            if (filter.getIsAvailable() != null && filter.getIsAvailable()) {
                predicates.add(criteriaBuilder.isTrue(
                        root.get("isAvailable")
                ));
            }

            // Verknüpfe alle gesammelten Predicates mit logischem AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
