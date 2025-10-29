package fhdw.de.einkauf_service.dto;

import lombok.Data;

/**
 * Data Transfer Object zum Halten der dynamischen Such- und Filterkriterien
 * für Artikel.
 */
@Data // Generiert automatisch Getter, Setter, toString, equals und hashCode
public class ArticleFilterDTO {

    // Suchfelder
    private String name;
    private String articleNumber;
    private String category;

    // Filterfelder
    private String supplier;
    private String manufacturer;

    // Statusfilter
    // Wird als Boolean (Wrapper-Klasse) definiert, um null zu erlauben, wenn der Filter nicht gesetzt ist.
    private Boolean isAvailable;

}
