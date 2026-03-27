package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shelf_level", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"shelf_id", "level_position"})
})
@EqualsAndHashCode(exclude = {"shelf", "placements"})
public class ShelfLevel {

    public ShelfLevel() {
    }

    public ShelfLevel(Long id, Integer levelPosition, Shelf shelf, List<ShelfPlacement> placements, LocalDateTime dateCreated) {
        this.id = id;
        this.levelPosition = levelPosition;
        this.shelf = shelf;
        this.placements = placements;
        this.dateCreated = dateCreated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLevelPosition() {
        return levelPosition;
    }

    public void setLevelPosition(Integer levelPosition) {
        this.levelPosition = levelPosition;
    }

    public Shelf getShelf() {
        return shelf;
    }

    public void setShelf(Shelf shelf) {
        this.shelf = shelf;
    }

    public List<ShelfPlacement> getPlacements() {
        return placements;
    }

    public void setPlacements(List<ShelfPlacement> placements) {
        this.placements = placements;
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

    // Position from bottom: 1 (bottom) to 5 (top)
    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer levelPosition;

    // Relationship: Level → Shelf
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelf_id", nullable = false)
    private Shelf shelf;

    // Relationship: Level → Placements
    @OneToMany(mappedBy = "shelfLevel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShelfPlacement> placements = new ArrayList<>();

    // Audit
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime dateCreated;
}
