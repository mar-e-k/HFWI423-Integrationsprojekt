package fhdw.de.einkauf_service.dto;

public class ContingentResponseDTO {
    public ContingentResponseDTO() {
    }

    public ContingentResponseDTO(Long id, Long orderId, Long supplierId, String supplierName, Long articleId, String articleName, Integer availableQuantity, Integer originalOrderQuantity) {
        this.id = id;
        this.orderId = orderId;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.articleId = articleId;
        this.articleName = articleName;
        this.availableQuantity = availableQuantity;
        this.originalOrderQuantity = originalOrderQuantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getArticleName() {
        return articleName;
    }

    public void setArticleName(String articleName) {
        this.articleName = articleName;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getOriginalOrderQuantity() {
        return originalOrderQuantity;
    }

    public void setOriginalOrderQuantity(Integer originalOrderQuantity) {
        this.originalOrderQuantity = originalOrderQuantity;
    }

    private Long id;
    private Long orderId;
    private Long supplierId;
    private String supplierName;
    private Long articleId;
    private String articleName;
    private Integer availableQuantity;
    private Integer originalOrderQuantity; // Die Menge aus der ursprünglichen OrderItem-Zeile

    /**
     * Berechnet den Prozentsatz des noch verfügbaren Kontingents
     */
    public Double getAvailabilityPercentage() {
        if (originalOrderQuantity == null || originalOrderQuantity <= 0) {
            return 0.0;
        }
        return (double) availableQuantity / originalOrderQuantity * 100;
    }

}
