package fhdw.de.einkauf_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ShelfRequestDTO {

    public ShelfRequestDTO(String name, String description, Long categoryId) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
    }

    public ShelfRequestDTO() {
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    @NotBlank(message = "Regal-Name ist erforderlich")
    @Size(min = 1, max = 100, message = "Regal-Name muss zwischen 1 und 100 Zeichen lang sein")
    private String name;

    @Size(max = 500, message = "Beschreibung darf maximal 500 Zeichen lang sein")
    private String description;

    @NotNull(message = "Kategorie-ID ist erforderlich")
    private Long categoryId;
}
