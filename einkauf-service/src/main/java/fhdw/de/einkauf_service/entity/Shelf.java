package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shelf")
@Data
@EqualsAndHashCode(exclude = "levels")
public class Shelf {

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
