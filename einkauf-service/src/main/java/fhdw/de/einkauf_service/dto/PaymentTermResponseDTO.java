package fhdw.de.einkauf_service.dto;

public class PaymentTermResponseDTO {
    public PaymentTermResponseDTO() {
    }

    public PaymentTermResponseDTO(Long id, String definition, String description) {
        this.id = id;
        this.definition = definition;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private Long id;
    private String definition;
    private String description;
}
