package fhdw.de.einkauf_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Table(name = "article")
@Data // Lombok: Generates Getters, Setters, toString, equals, and hashCode
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Required Field Validation & Unique Constraint (GTIN)
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Article number (GTIN) is mandatory.")
    @Size(min = 8, max = 18, message = "GTIN must be between 8 and 18 characters.")
    private String articleNumber;

    @Column(nullable = false)
    @NotBlank(message = "Article name is mandatory.")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Unit is mandatory.")
    private String unit;

    // Price Fields with Validation
    @Column(nullable = false)
    @NotNull(message = "Purchase price is mandatory.")
    @Positive(message = "Purchase price must be positive.")
    private Double purchasePrice;

    @Column(nullable = false)
    @NotNull(message = "Tax rate is mandatory.")
    @Min(value = 0, message = "Tax rate cannot be negative.")
    @Max(value = 100, message = "Tax rate cannot exceed 100%.")
    private Double taxRatePercent;

    // Calculated field (will be set in Service layer, but persisted)
    @Column(nullable = false)
    private Double sellingPrice;

    @Column(nullable = false)
    @NotBlank(message = "Manufacturer is mandatory.")
    private String manufacturer;

    @Column(nullable = false)
    @NotBlank(message = "Supplier is mandatory.")
    private String supplier;

    @NotNull(message = "Stock level is mandatory.")
    @Min(value = 0, message = "Stock level cannot be negative.")
    private Integer stockLevel;

    @Column(length = 1024)
    private String description;
}