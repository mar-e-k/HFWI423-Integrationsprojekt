package fhdw.de.einkauf_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShelfRequestDTO {

    @NotBlank(message = "Regal-Name ist erforderlich")
    @Size(min = 1, max = 100, message = "Regal-Name muss zwischen 1 und 100 Zeichen lang sein")
    private String name;

    @Size(max = 500, message = "Beschreibung darf maximal 500 Zeichen lang sein")
    private String description;

    @NotNull(message = "Kategorie-ID ist erforderlich")
    private Long categoryId;
}
