package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shelf")
@EqualsAndHashCode(exclude = "levels")
public class Shelf {

    public Shelf() {
    }

    public Shelf(Long id, Double widthCm, Double heightCm, Double depthCm, String name, String description, Category category, Set<ShelfLevel> levels, LocalDateTime dateCreated, LocalDateTime dateUpdated) {
        this.id = id;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.depthCm = depthCm;
        this.name = name;
        this.description = description;
        this.category = category;
        this.levels = levels;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<ShelfLevel> getLevels() {
        return levels;
    }

    public void setLevels(Set<ShelfLevel> levels) {
        this.levels = levels;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fixed dimensions (constants for all shelves)
    @Column(nullable = false)
    private Double widthCm = 100.0;      // Fixed: 100cm

    @Column(nullable = false)
    private Double heightCm = 150.0;     // Fixed: 150cm

    @Column(nullable = false)
    private Double depthCm = 47.0;       // Fixed: 47cm

    // Shelf properties
    @Column(nullable = false)
    private String name;                 // e.g., "Make-Up Regal 1"

    @Column(length = 500)
    private String description;

    // Relationship: Shelf → Category (Many Shelves per Category)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Relationship: Shelf → Levels
    @OneToMany(mappedBy = "shelf", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ShelfLevel> levels = new HashSet<>();

    // Audit
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime dateCreated;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime dateUpdated;
}
