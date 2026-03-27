package fhdw.de.einkauf_service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

// Dieses DTO benötigt keine Validierungs-Annotationen, da es nur ausgegeben wird.
public class ArticleResponseDTO {
    public ArticleResponseDTO(Long id, String articleNumber, String name, Double purchasePrice, Double taxRatePercent, Double sellingPrice, String manufacturer, Set<SupplierResponseDTO> suppliers, SupplierResponseDTO mainSupplier, Integer stockLevel, String description, Boolean isAvailable, Boolean hasDeposit, Set<CategoryResponseDTO> categoryIds, String productImage, LocalDateTime dateCreated, LocalDate expirationDate, Double widthCm, Double heightCm, Double depthCm) {
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
        this.categoryIds = categoryIds;
        this.productImage = productImage;
        this.dateCreated = dateCreated;
        this.expirationDate = expirationDate;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.depthCm = depthCm;
    }

    public ArticleResponseDTO() {
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

    public Set<SupplierResponseDTO> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(Set<SupplierResponseDTO> suppliers) {
        this.suppliers = suppliers;
    }

    public SupplierResponseDTO getMainSupplier() {
        return mainSupplier;
    }

    public void setMainSupplier(SupplierResponseDTO mainSupplier) {
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

    public Set<CategoryResponseDTO> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<CategoryResponseDTO> categoryIds) {
        this.categoryIds = categoryIds;
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

    // Eindeutige ID (vom Backend generiert)
    private Long id;

    private String articleNumber;
    private String name;
    private Double purchasePrice;
    private Double taxRatePercent;
    private Double sellingPrice;
    private String manufacturer;
    private Set<SupplierResponseDTO> suppliers;
    private SupplierResponseDTO mainSupplier;
    private Integer stockLevel;
    private String description;
    private Boolean isAvailable;
    private Boolean hasDeposit;
    private Set<CategoryResponseDTO> categoryIds;
    private String productImage;
    private LocalDateTime dateCreated;
    private LocalDate expirationDate;
    private Double widthCm;
    private Double heightCm;
    private Double depthCm;
}