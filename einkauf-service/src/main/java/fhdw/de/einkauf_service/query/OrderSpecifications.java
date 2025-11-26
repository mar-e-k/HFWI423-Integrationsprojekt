package fhdw.de.einkauf_service.query;

import fhdw.de.einkauf_service.dto.OrderFilterDTO;
import fhdw.de.einkauf_service.entity.Order;
import fhdw.de.einkauf_service.entity.OrderItem;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecifications {

    public static Specification<Order> filterOrders(OrderFilterDTO filter) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtern nach BESTELLNUMMER (LIKE %number%)
            if (StringUtils.hasText(filter.getOrderNumber())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("orderNumber")),
                        "%" + filter.getOrderNumber().toLowerCase() + "%"
                ));
            }

            // 2. Filtern nach STATUS (Exakte Übereinstimmung)
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("status"),
                        filter.getStatus()
                ));
            }

            // 3. Filtern nach LIEFERANTEN-ID (Exakte Übereinstimmung)
            if (filter.getSupplierId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("supplier").get("id"), // Join auf die Supplier-Entity
                        filter.getSupplierId()
                ));
            }

            // 4. Filtern nach OrderDate
            // A) Startdatum (Von-Datum)
            if (filter.getOrderDateFrom() != null) {
                // Wir nehmen den Anfang des Start-Tages (00:00:00)
                LocalDateTime startDateTime = filter.getOrderDateFrom().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("orderDate"),
                        startDateTime
                ));
            }

            // B) Enddatum (Bis-Datum)
            if (filter.getOrderDateTo() != null) {
                // Wir nehmen das Ende des End-Tages (23:59:59.999...).
                LocalDateTime endDateTime = filter.getOrderDateTo().plusDays(1).atStartOfDay();
                predicates.add(criteriaBuilder.lessThan(
                        root.get("orderDate"),
                        endDateTime
                ));
            }

            // 5. Filtern nach ARTIKEL (über OrderItem)
            if (filter.getArticleId() != null) {

                // Erstelle Subquery, um alle Order-IDs zu finden, die den gesuchten Artikel enthalten
                jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);

                // Root des Subquerys ist OrderItem
                jakarta.persistence.criteria.Root<OrderItem> orderItemRoot = subquery.from(OrderItem.class);

                // Select: Order-ID aus der OrderItem-Tabelle
                subquery.select(orderItemRoot.get("order").get("id"));

                // Where: article_id in OrderItem == gesuchte ID
                subquery.where(criteriaBuilder.equal(
                        orderItemRoot.get("article").get("id"),
                        filter.getArticleId()
                ));

                // Füge die Bedingung zur Hauptabfrage hinzu: Order-ID muss in der Subquery-Liste sein
                predicates.add(criteriaBuilder.in(root.get("id")).value(subquery));
            }

            // Verknüpfe alle Predicates mit logischem AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
