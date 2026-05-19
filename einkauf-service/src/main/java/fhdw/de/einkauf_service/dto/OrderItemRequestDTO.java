package fhdw.de.einkauf_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Eine Position einer (Wieder-)Bestellung")
public record OrderItemRequestDTO(
        @Schema(description = "ID des Artikels", example = "42") Long articleId,
        @Schema(description = "Menge", example = "3") Integer quantity
) {}
