package fhdw.de.einkauf_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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

    @NotBlank(message = "Category name is mandatory.")
    private String name;

    private String description;
}
