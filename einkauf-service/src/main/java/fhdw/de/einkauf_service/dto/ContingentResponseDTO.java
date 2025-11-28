package fhdw.de.einkauf_service.dto;

import lombok.Data;

@Data
public class ContingentResponseDTO {

    private Long id;
    private Long orderId;
    private Long supplierId;
    private String supplierName;
    private Long articleId;
    private String articleName;
    private Integer availableQuantity;
    private Integer originalOrderQuantity; // Die Menge aus der ursprünglichen OrderItem-Zeile

    /**
     * Berechnet den Prozentsatz des noch verfügbaren Kontingents
     */
    public Double getAvailabilityPercentage() {
        if (originalOrderQuantity == null || originalOrderQuantity <= 0) {
            return 0.0;
        }
        return (double) availableQuantity / originalOrderQuantity * 100;
    }

}
