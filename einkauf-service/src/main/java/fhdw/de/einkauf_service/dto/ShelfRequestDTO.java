package fhdw.de.einkauf_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Eingabe-DTO zum Anlegen oder Aktualisieren eines Regals")
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

    @Schema(description = "Regalname", example = "Regal A1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Regal-Name ist erforderlich")
    @Size(min = 1, max = 100, message = "Regal-Name muss zwischen 1 und 100 Zeichen lang sein")
    private String name;

    @Schema(description = "Optionale Beschreibung", example = "Hauptregal Getränke")
    @Size(max = 500, message = "Beschreibung darf maximal 500 Zeichen lang sein")
    private String description;

    @Schema(description = "ID der Kategorie, der das Regal zugeordnet ist", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Kategorie-ID ist erforderlich")
    private Long categoryId;
}
