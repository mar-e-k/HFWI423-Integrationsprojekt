package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "shelf_placement")
@Data
public class ShelfPlacement {

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
