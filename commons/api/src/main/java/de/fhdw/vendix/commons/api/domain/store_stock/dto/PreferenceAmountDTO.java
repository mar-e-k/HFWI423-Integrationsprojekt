package de.fhdw.vendix.commons.api.domain.store_stock.dto;

import de.fhdw.vendix.commons.api.structure.dto.EmbeddableDTO;

public record PreferenceAmountDTO(
        long minimum,
        long average,
        long max
) implements EmbeddableDTO {
    public PreferenceAmountDTO {
        if (minimum < 0) {
            throw new IllegalArgumentException("PreferenceAmountDTO parameter 'minimum' cannot be negative");
        }
        if (average < 0) {
            throw new IllegalArgumentException("PreferenceAmountDTO parameter 'average' cannot be negative");
        }
        if (max < 0) {
            throw new IllegalArgumentException("PreferenceAmountDTO parameter 'max' cannot be negative");
        }
        if (minimum > average) {
            throw new IllegalArgumentException("PreferenceAmountDTO parameter 'minimum' cannot be greater than the average amount");
        }
        if (minimum > max) {
            throw new IllegalArgumentException("PreferenceAmountDTO parameter 'minimum' cannot be greater than max");
        }
        if (average > max) {
            throw new IllegalArgumentException("PreferenceAmountDTO parameter 'average' cannot be greater than max");
        }
    }
}