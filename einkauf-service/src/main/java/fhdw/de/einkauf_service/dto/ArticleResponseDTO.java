package fhdw.de.einkauf_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

// Dieses DTO benötigt keine Validierungs-Annotationen, da es nur ausgegeben wird.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponseDTO {

    // Eindeutige ID (vom Backend generiert)
    private Long id;

    private String articleNumber;
    private String name;
    private Double purchasePrice;
    private Double taxRatePercent;
    private Double sellingPrice;
    private String manufacturer;
    private Long supplierId;
    private String supplierName;
    private Integer stockLevel;
    private String description;
    private Boolean isAvailable;
    private Boolean hasDeposit;
    private Set<CategoryResponseDTO> categoryIds;
}