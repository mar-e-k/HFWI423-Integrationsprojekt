package fhdw.de.einkauf_service.dto;

import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object zum Halten der dynamischen Such- und Filterkriterien
 * für Artikel.
 */
@Data // Generiert automatisch Getter, Setter, toString, equals und hashCode
public class ArticleFilterDTO {

    // Suchfelder
    private String name;
    private String articleNumber;


    // Filterfelder
    private Long supplierId;
    private String manufacturer;
    private List<Long> categoryIds;

    // Statusfilter
    // Wird als Boolean definiert, um null zu erlauben, wenn der Filter nicht gesetzt ist.
    private Boolean isAvailable;

}
