package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shelf_placement")
public class ShelfPlacement {

    public ShelfPlacement() {
    }

    public ShelfPlacement(Long id, ShelfLevel shelfLevel, Article article, Double positionX, Double positionY, Double widthCm, Double heightCm, LocalDateTime dateCreated) {
        this.id = id;
        this.shelfLevel = shelfLevel;
        this.article = article;
        this.positionX = positionX;
        this.positionY = positionY;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.dateCreated = dateCreated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ShelfLevel getShelfLevel() {
        return shelfLevel;
    }

    public void setShelfLevel(ShelfLevel shelfLevel) {
        this.shelfLevel = shelfLevel;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY;
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

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship: Placement → Level
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelf_level_id", nullable = false)
    private ShelfLevel shelfLevel;

    // Relationship: Placement → Article
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    // Position on level: X coordinate from left (cm)
    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Double positionX;

    // Position on level: Y coordinate from bottom (cm)
    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Double positionY;

    // Article dimensions on shelf
    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Double widthCm;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Double heightCm;

    // Audit
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime dateCreated;
}
