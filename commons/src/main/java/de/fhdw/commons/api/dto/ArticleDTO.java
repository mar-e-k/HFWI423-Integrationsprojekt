package de.fhdw.commons.api.dto;

public class ArticleDTO extends AbstractDTO<Long> {

    private String articleNumber;
    private String description;
    private String manufacturer;
    private String name;
    private Double purchasePrice;
    private Double sellingPrice;
    private Integer stockLevel;
    private String supplier;
    private Double taxRatePercent;
    private String unit;
    private Boolean isAvailable;

    public ArticleDTO() {
        super();
    }

    public ArticleDTO(Long aLong, String articleNumber, String description, String manufacturer, String name, Double purchasePrice, Double sellingPrice, Integer stockLevel, String supplier, Double taxRatePercent, String unit, Boolean isAvailable) {
        super(aLong);
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

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
}
