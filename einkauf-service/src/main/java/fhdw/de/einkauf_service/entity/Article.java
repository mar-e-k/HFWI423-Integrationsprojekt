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
public class Article {
    public Article() {
    }

    public Article(Long id, String articleNumber, String name, double purchasePrice, Double taxRatePercent, Double sellingPrice, String manufacturer, Set<Supplier> suppliers, Supplier mainSupplier, Integer stockLevel, String description, Boolean isAvailable, Boolean hasDeposit, Set<Category> categories, String productImage, LocalDateTime dateCreated, LocalDate expirationDate, Double widthCm, Double heightCm, Double depthCm) {
        this.id = id;
        this.articleNumber = articleNumber;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.taxRatePercent = taxRatePercent;
        this.sellingPrice = sellingPrice;
        this.manufacturer = manufacturer;
        this.suppliers = suppliers;
        this.mainSupplier = mainSupplier;
        this.stockLevel = stockLevel;
        this.description = description;
        this.isAvailable = isAvailable;
        this.hasDeposit = hasDeposit;
        this.categories = categories;
        this.productImage = productImage;
        this.dateCreated = dateCreated;
        this.expirationDate = expirationDate;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.depthCm = depthCm;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Double getTaxRatePercent() {
        return taxRatePercent;
    }

    public void setTaxRatePercent(Double taxRatePercent) {
        this.taxRatePercent = taxRatePercent;
    }

    public Double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Set<Supplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(Set<Supplier> suppliers) {
        this.suppliers = suppliers;
    }

    public Supplier getMainSupplier() {
        return mainSupplier;
    }

    public void setMainSupplier(Supplier mainSupplier) {
        this.mainSupplier = mainSupplier;
    }

    public Integer getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    public Boolean getHasDeposit() {
        return hasDeposit;
    }

    public void setHasDeposit(Boolean hasDeposit) {
        this.hasDeposit = hasDeposit;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Double getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(Double widthCm) {
        this.widthCm = widthCm;
    }

    public Double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
    }

    public Double getDepthCm() {
        return depthCm;
    }

    public void setDepthCm(Double depthCm) {
        this.depthCm = depthCm;
    }

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

    @ManyToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "connector_article_supplier",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "supplier_id")
    )
    private Set<Supplier> suppliers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Supplier mainSupplier;

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