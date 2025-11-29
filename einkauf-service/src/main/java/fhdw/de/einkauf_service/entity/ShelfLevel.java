package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shelf_level", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"shelf_id", "level_position"})
})
@Data
public class ShelfLevel {

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
