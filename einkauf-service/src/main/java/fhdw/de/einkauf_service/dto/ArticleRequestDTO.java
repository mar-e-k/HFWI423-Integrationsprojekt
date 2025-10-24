package fhdw.de.einkauf_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//hier dto unbenennen
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequestDTO {

    // Required Field Validation & Unique Constraint (GTIN)
    @NotBlank(message = "Article number (GTIN) is mandatory.")
    @Size(min = 8, max = 18, message = "GTIN must be between 8 and 18 characters.")
    private String articleNumber;

    @NotBlank(message = "Article name is mandatory.")
    private String name;

    @NotBlank(message = "Unit is mandatory.")
    private String unit;

    @NotNull(message = "Purchase price is mandatory.")
    @Positive(message = "Purchase price must be positive.")
    private Double purchasePrice;

    @NotNull(message = "Tax rate is mandatory.")
    @Min(value = 0, message = "Tax rate cannot be negative.")
    @Max(value = 100, message = "Tax rate cannot exceed 100%.")
    private Double taxRatePercent;

    @NotBlank(message = "Manufacturer is mandatory.")
    private String manufacturer;

    @NotBlank(message = "Supplier is mandatory.")
    private String supplier;

    @NotNull(message = "Stock level is mandatory.")
    @Min(value = 0, message = "Stock level cannot be negative.")
    private Integer stockLevel;

    @Size(max = 1024, message = "Description cannot exceed 1024 characters.")
    private String description;
}