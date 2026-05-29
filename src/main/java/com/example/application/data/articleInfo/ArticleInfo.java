package com.example.application.data.articleInfo;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "article_info", schema = "artikel", indexes = {
    @Index(name = "idx_article_info_number",   columnList = "article_number"),
    @Index(name = "idx_article_info_id",       columnList = "article_id"),
    @Index(name = "idx_article_info_location", columnList = "storage_location")
})
public class ArticleInfo extends AbstractEntity {

    @Column(name = "article_id")
    private Long articleId;

    // Name des Artikels
    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;
    // Artikelnummer
    @Size(max = 18)
    @NotNull
    @Column(name = "article_number", nullable = false, length = 18)
    private String articleNumber;
    // Lagerbestand des Artikels
    @NotNull
    @Column(name = "stock_level", nullable = false)
    private Integer stockLevel;
    // Lagerort des Artikels
    @Size(max = 255)
    @NotNull
    @Column(name = "storage_location", nullable = false)
    private String storageLocation;

    @Size(max = 255)
    @Column(name = "reserve_storage_location")
    private String reserveStorageLocation;

    // Mindestbestand des Artikels (ab wann nachbestellt werden muss)
    //@NotNull
    @Column(name = "min_stock")//, nullable = false
    private Integer minStock;

    //Stückzahl pro Palette (eigentlich vom Einkauf aber erstmal wir)
    // wird nicht im Grid angezeigt
    @Column(name = "pieces_per_pallet")
    private Integer piecesPerPallet;

    //Anzahl ungeöffneter Paletten im Reservefach
    @Column(name = "reserve_pallets")
    private Integer reservePallets;

    // Getter Setter -----------------------------------------

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

    public Integer getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(Integer stockLevel) {
        this.stockLevel = stockLevel;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public Integer getMinStock() {
        return minStock;
    }

    public void setMinStock(Integer minStock) {
        this.minStock = minStock;
    }

    public String getReserveStorageLocation() {
        return reserveStorageLocation;
    }

    public void setReserveStorageLocation(String reserveStorageLocation) {
        this.reserveStorageLocation = reserveStorageLocation;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Integer getPiecesPerPallet() {
        return piecesPerPallet;
    }

    public void setPiecesPerPallet(Integer piecesPerPallet) {
        this.piecesPerPallet = piecesPerPallet;
    }

    public Integer getReservePallets() {
        return reservePallets;
    }

    public void setReservePallets(Integer reservePallets) {
        this.reservePallets = reservePallets;
    }

    @Transient
    public Integer getTotalStock() {
        int open = stockLevel != null ? stockLevel : 0;
        int perPallet = piecesPerPallet != null ? piecesPerPallet : 0;
        int pallets = reservePallets != null ? reservePallets : 0;
        return open + perPallet * pallets;
    }
}
