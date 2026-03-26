package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "category")
@EqualsAndHashCode(exclude = "articles")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Category() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Category name is mandatory.")
    private String name;

    private String description; // Optional: Zusätzliche Beschreibung

    // Bidirektionale Beziehung zur Article-Entity
    // 'mappedBy' gibt an, dass die Join-Tabelle in der Article-Entity definiert ist
    @ManyToMany(mappedBy = "categories")
    private Set<Article> articles = new HashSet<>();
}
