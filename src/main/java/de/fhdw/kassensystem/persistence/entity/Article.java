package de.fhdw.kassensystem.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "article")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 18)
    @NotNull
    @Column(name = "article_number", nullable = false, length = 18)
    private String articleNumber;

    @Size(max = 1024)
    @Column(name = "description", length = 1024)
    private String description;

    @Size(max = 255)
    @NotNull
    @Column(name = "manufacturer", nullable = false)
    private String manufacturer;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "purchase_price", nullable = false)
    private Double purchasePrice;

    @NotNull
    @Column(name = "selling_price", nullable = false)
    private Double sellingPrice;

    @NotNull
    @Column(name = "stock_level", nullable = false)
    private Integer stockLevel;

    @Size(max = 255)
    @NotNull
    @Column(name = "supplier", nullable = false)
    private String supplier;

    @NotNull
    @Column(name = "tax_rate_percent", nullable = false)
    private Double taxRatePercent;

    @Size(max = 255)
    @NotNull
    @Column(name = "unit", nullable = false)
    private String unit;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = false;

    public Article() {
        super();
    }

    public Article(Long id, String articleNumber, String description, String manufacturer, String name, Double purchasePrice, Double sellingPrice, Integer stockLevel, String supplier, Double taxRatePercent, String unit, Boolean isAvailable) {
        this.id = id;
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
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Double getTaxRatePercent() {
        return taxRatePercent;
    }

    public void setTaxRatePercent(Double taxRatePercent) {
        this.taxRatePercent = taxRatePercent;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", articleNumber='" + articleNumber + '\'' +
                ", description='" + description + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", name='" + name + '\'' +
                ", purchasePrice=" + purchasePrice +
                ", sellingPrice=" + sellingPrice +
                ", stockLevel=" + stockLevel +
                ", supplier='" + supplier + '\'' +
                ", taxRatePercent=" + taxRatePercent +
                ", unit='" + unit + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}