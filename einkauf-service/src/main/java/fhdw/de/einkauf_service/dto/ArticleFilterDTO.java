package fhdw.de.einkauf_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Objects;

@Schema(description = "Filterkriterien für die Artikelsuche (Query-Parameter)")
public class ArticleFilterDTO {

    // Suchfelder
    private String name;
    private String articleNumber;


    // Filterfelder
    private Long supplierId;
    private String manufacturer;
    private List<Long> categoryIds;

    // Statusfilter
    // Wird als Boolean definiert, um null zu erlauben, wenn der Filter nicht gesetzt ist.
    private Boolean isAvailable;

    public ArticleFilterDTO() {
    }

    public ArticleFilterDTO(String name, String articleNumber, Long supplierId, String manufacturer, List<Long> categoryIds, Boolean isAvailable) {
        this.name = name;
        this.articleNumber = articleNumber;
        this.supplierId = supplierId;
        this.manufacturer = manufacturer;
        this.categoryIds = categoryIds;
        this.isAvailable = isAvailable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArticleFilterDTO that)) return false;
        return Objects.equals(name, that.name)
                && Objects.equals(articleNumber, that.articleNumber)
                && Objects.equals(supplierId, that.supplierId)
                && Objects.equals(manufacturer, that.manufacturer)
                && Objects.equals(categoryIds, that.categoryIds)
                && Objects.equals(isAvailable, that.isAvailable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, articleNumber, supplierId, manufacturer, categoryIds, isAvailable);
    }
}
