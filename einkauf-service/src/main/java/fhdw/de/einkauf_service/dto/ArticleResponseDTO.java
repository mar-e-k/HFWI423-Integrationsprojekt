package fhdw.de.einkauf_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Dieses DTO benötigt keine Validierungs-Annotationen, da es nur ausgegeben wird.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponseDTO {

    // Eindeutige ID (vom Backend generiert)
    private Long id;

    private String articleNumber;
    private String name;
    private String unit;
    private Double purchasePrice;
    private Double taxRatePercent;

    // Berechneter Preis (vom Backend generiert)
    private Double sellingPrice;

    private String manufacturer;
    private String supplier;
    private Integer stockLevel;
    private String description;
}