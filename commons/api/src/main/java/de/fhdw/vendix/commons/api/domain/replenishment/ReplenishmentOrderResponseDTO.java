package de.fhdw.vendix.commons.api.domain.replenishment;

import de.fhdw.vendix.commons.api.structure.dto.ResponseDTO;

import java.util.UUID;

public record ReplenishmentOrderResponseDTO(
        UUID correlationId,
        ReplenishmentOrderStatus status,
        String statusUrl
) implements ResponseDTO {

    public ReplenishmentOrderResponseDTO {
        if (correlationId == null) {
            throw new IllegalArgumentException("ReplenishmentOrderResponseDTO: 'correlationId' cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("ReplenishmentOrderResponseDTO: 'status' cannot be null");
        }
        if (statusUrl == null || statusUrl.isBlank()) {
            throw new IllegalArgumentException("ReplenishmentOrderResponseDTO: 'statusUrl' cannot be null or blank");
        }
    }
}
