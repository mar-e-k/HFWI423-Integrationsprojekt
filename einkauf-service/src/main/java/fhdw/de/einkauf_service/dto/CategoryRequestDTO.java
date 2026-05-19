package fhdw.de.einkauf_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Eingabe-DTO zum Anlegen oder Aktualisieren einer Artikel-Kategorie")
public class CategoryRequestDTO {
    public String getName() {
        return name;
    }

    public CategoryRequestDTO() {
    }

    public CategoryRequestDTO(String name, String description) {
        this.name = name;
        this.description = description;
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

    @Schema(description = "Name der Kategorie", example = "Getränke", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Category name is mandatory.")
    private String name;

    @Schema(description = "Optionale Beschreibung", example = "Alle alkoholfreien und alkoholischen Getränke")
    private String description;
}
