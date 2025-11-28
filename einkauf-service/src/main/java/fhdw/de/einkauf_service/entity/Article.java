package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "article")
@Data
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    @NotBlank(message = "Article number (GTIN) is mandatory.")
    @Size(min = 8, max = 18, message = "GTIN must be between 8 and 18 characters.")
    private String articleNumber;

    @Column(nullable = false)
    @NotBlank(message = "Article name is mandatory.")
    private String name;


    @Column(nullable = false)
    @NotNull(message = "Purchase price is mandatory.")
    @Positive(message = "Purchase price must be positive.")
    private double purchasePrice;

    @Column(nullable = false)
    @NotNull(message = "Tax rate is mandatory.")
    @Min(value = 0, message = "Tax rate cannot be negative.")
    @Max(value = 100, message = "Tax rate cannot exceed 100%.")
    private Double taxRatePercent;

    @Column(nullable = false)
    private Double sellingPrice;

    @Column(nullable = false)
    @NotBlank(message = "Manufacturer is mandatory.")
    private String manufacturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @NotNull(message = "Stock level is mandatory.")
    @Min(value = 0, message = "Stock level cannot be negative.")
    private Integer stockLevel;

    @Column(length = 1024)
    private String description;

    @Column(nullable = false)
    private Boolean isAvailable = false;

    @Column(nullable = false)
    private Boolean hasDeposit;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })

    @JoinTable(
            name = "connector_article_category",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @Column(length = 2048)
    private String productImage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    private LocalDate expirationDate;

    @Column(nullable = false)
    @NotNull(message = "Width is mandatory.")
    @Positive(message = "Width must be positive.")
    private Double widthCm;

    @Column(nullable = false)
    @NotNull(message = "Height is mandatory.")
    @Positive(message = "Height must be positive.")
    private Double heightCm;

    @Column(nullable = false)
    @NotNull(message = "Depth is mandatory.")
    @Positive(message = "Depth must be positive.")
    private Double depthCm;

    @PrePersist
    protected void onCreate() {
        this.dateCreated = LocalDateTime.now();
    }
}