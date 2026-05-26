package de.fhdw.vendix.store.core.domain.article;

import de.fhdw.vendix.commons.api.structure.mapper.Default;
import de.fhdw.vendix.commons.spring.data.entity.AbstractSpringDataEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Immutable;
import org.jspecify.annotations.Nullable;

/**
 * External Read-Only import from DB
 */
@Entity
@Table(indexes = {
        @Index(name = "idx_article_article_number", columnList = "article_number")
})
@Immutable
public class Article extends AbstractSpringDataEntity<Long> {

    @Column(name = "article_number", nullable = false, length = 18)
    @NotBlank(message = "Article number cannot be blank")
    @Size(max = 18, message = "Article number must be at most 18 characters")
    private String articleNumber;

    @Size(max = 1024, message = "Description must be at most 1024 characters")
    @Column(name = "description", length = 1024)
    private String description;

    @NotBlank(message = "Manufacturer cannot be blank")
    @Size(max = 255, message = "Manufacturer must be at most 255 characters")
    @Column(name = "manufacturer", nullable = false)
    private String manufacturer;

    @Column(name = "name", nullable = false)
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @Column(name = "purchase_price", nullable = false)
    @NotNull(message = "Purchase price cannot be null")
    @PositiveOrZero(message = "Purchase price must be positive or zero")
    private Double purchasePrice;

    @Column(name = "selling_price", nullable = false)
    @NotNull(message = "Selling price cannot be null")
    @PositiveOrZero(message = "Selling price must be positive or zero")
    private Double sellingPrice;

    @Column(name = "stock_level", nullable = false)
    @NotNull(message = "Stock level cannot be null")
    @PositiveOrZero(message = "Stock level must be positive or zero")
    private Integer stockLevel;

    @Column(name = "supplier", nullable = false)
    @NotBlank(message = "Supplier cannot be blank")
    @Size(max = 255, message = "Supplier must be at most 255 characters")
    private String supplier;

    @NotNull(message = "Tax rate percent cannot be null")
    @PositiveOrZero(message = "Tax rate percent must be positive or zero")
    @Column(name = "tax_rate_percent", nullable = false)
    private Double taxRatePercent;

    @NotBlank(message = "Unit cannot be blank")
    @Size(max = 255, message = "Unit must be at most 255 characters")
    @Column(name = "unit", nullable = false)
    private String unit;

    @NotNull(message = "Is available cannot be null")
    @ColumnDefault("true")
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = false;

    @NotNull(message = "Has deposit cannot be null")
    @ColumnDefault("false")
    @Column(name = "has_deposit", nullable = false)
    private boolean hasDeposit = false;

    protected Article() {}

    protected Article(
            String articleNumber,
            String description,
            String manufacturer,
            String name,
            Double purchasePrice,
            Double sellingPrice,
            Integer stockLevel,
            String supplier,
            Double taxRatePercent,
            String unit,
            Boolean isAvailable,
            boolean hasDeposit
    ) {
        this.articleNumber = articleNumber;
        this.description = description;
        this.manufacturer = manufacturer;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.stockLevel = stockLevel;
        this.supplier = supplier;
        this.taxRatePercent = taxRatePercent;
        this.unit = unit;
        this.isAvailable = isAvailable;
        this.hasDeposit = hasDeposit;
    }

    @Default
    protected Article(
            @Nullable Long id,
            String articleNumber,
            String description,
            String manufacturer,
            String name,
            Double purchasePrice,
            Double sellingPrice,
            Integer stockLevel,
            String supplier,
            Double taxRatePercent,
            String unit,
            Boolean isAvailable,
            boolean hasDeposit
    ) {
        super(id);
        this.articleNumber = articleNumber;
        this.description = description;
        this.manufacturer = manufacturer;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.stockLevel = stockLevel;
        this.supplier = supplier;
        this.taxRatePercent = taxRatePercent;
        this.unit = unit;
        this.isAvailable = isAvailable;
        this.hasDeposit = hasDeposit;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public String getDescription() {
        return description;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getName() {
        return name;
    }

    public Double getPurchasePrice() {
        return purchasePrice;
    }

    public Double getSellingPrice() {
        return sellingPrice;
    }

    public Integer getStockLevel() {
        return stockLevel;
    }

    public String getSupplier() {
        return supplier;
    }

    public Double getTaxRatePercent() {
        return taxRatePercent;
    }

    public String getUnit() {
        return unit;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public boolean isHasDeposit() {
        return hasDeposit;
    }
}
