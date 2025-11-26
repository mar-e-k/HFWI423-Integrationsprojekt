package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "category")
@Data
@EqualsAndHashCode(exclude = "articles")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Category name is mandatory.")
    private String name;

    private String description; // Optional: Zusätzliche Beschreibung

    // Bidirektionale Beziehung zur Article-Entity
    // 'mappedBy' gibt an, dass die Join-Tabelle in der Article-Entity definiert ist
    @ManyToMany(mappedBy = "categories")
    private Set<Article> articles = new HashSet<>();
}
