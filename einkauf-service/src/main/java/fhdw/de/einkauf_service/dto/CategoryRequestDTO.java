package fhdw.de.einkauf_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequestDTO {

    @NotBlank(message = "Category name is mandatory.")
    private String name;

    private String description;
}
