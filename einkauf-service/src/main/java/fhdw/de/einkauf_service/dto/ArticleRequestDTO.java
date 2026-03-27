package fhdw.de.einkauf_service.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

public class ArticleRequestDTO {
    public ArticleRequestDTO(String articleNumber, String name, Double purchasePrice, Double taxRatePercent, Double sellingPrice, String manufacturer, Set<Long> supplierIds, Long mainSupplierId, Integer stockLevel, String description, Boolean isAvailable, Boolean hasDeposit, Set<Long> categoryIds, String productImage, LocalDate expirationDate, Double widthCm, Double heightCm, Double depthCm) {

        this.articleNumber = articleNumber;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.taxRatePercent = taxRatePercent;
        this.sellingPrice = sellingPrice;
        this.manufacturer = manufacturer;
        this.supplierIds = supplierIds;
        this.mainSupplierId = mainSupplierId;
        this.stockLevel = stockLevel;
        this.description = description;
        this.isAvailable = isAvailable;
        this.hasDeposit = hasDeposit;
        this.categoryIds = categoryIds;
        this.productImage = productImage;
        this.expirationDate = expirationDate;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.depthCm = depthCm;
    }

    public ArticleRequestDTO() {
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

    public Double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Double purchasePrice) {
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

    public Set<Long> getSupplierIds() {
        return supplierIds;
    }

    public void setSupplierIds(Set<Long> supplierIds) {
        this.supplierIds = supplierIds;
    }

    public Long getMainSupplierId() {
        return mainSupplierId;
    }

    public void setMainSupplierId(Long mainSupplierId) {
        this.mainSupplierId = mainSupplierId;
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

    public Set<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
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

    // Required Field Validation & Unique Constraint (GTIN)
    @NotBlank(message = "Article number (GTIN) is mandatory.")
    @Size(min = 8, max = 18, message = "GTIN must be between 8 and 18 characters.")
    private String articleNumber;

    @NotBlank(message = "Article name is mandatory.")
    private String name;

    @NotNull(message = "Purchase price is mandatory.")
    @Positive(message = "Purchase price must be positive.")
    private Double purchasePrice;

    @NotNull(message = "Tax rate is mandatory.")
    @Min(value = 0, message = "Tax rate cannot be negative.")
    @Max(value = 100, message = "Tax rate cannot exceed 100%.")
    private Double taxRatePercent;

    @NotNull(message = "Selling price is mandatory.")
    @Positive(message = "Selling price must be positive.")
    private Double sellingPrice;

    @NotBlank(message = "Manufacturer is mandatory.")
    private String manufacturer;

    @NotNull(message = "Supplier ID must not be null.")
    private Set<Long> supplierIds;

    @NotNull(message = "Main supplier must not be null.")
    private Long mainSupplierId;

    @NotNull(message = "Stock level is mandatory.")
    @Min(value = 0, message = "Stock level cannot be negative.")
    private Integer stockLevel;

    @Size(max = 1024, message = "Description cannot exceed 1024 characters.")
    private String description;

    @NotNull(message = "Availability is mandatory.")
    private Boolean isAvailable;

    @NotNull(message = "Deposit information is mandatory.")
    private Boolean hasDeposit;

    @NotNull(message = "Category IDs set must not be null.")
    private Set<Long> categoryIds;

    private String productImage;

    private LocalDate expirationDate;

    @NotNull(message = "Width (Cm) is mandatory.")
    @Positive(message = "Width must be positive.")
    private Double widthCm;

    @NotNull(message = "Height (Cm) is mandatory.")
    @Positive(message = "Height must be positive.")
    private Double heightCm;

    @NotNull(message = "Depth (Cm) is mandatory.")
    @Positive(message = "Depth must be positive.")
    private Double depthCm;
}