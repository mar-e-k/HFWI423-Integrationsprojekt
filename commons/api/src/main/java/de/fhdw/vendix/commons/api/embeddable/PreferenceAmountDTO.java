package de.fhdw.vendix.commons.api.embeddable;

import de.fhdw.vendix.commons.api.structure.dto.EmbeddableDTO;

public record PreferenceAmountDTO(
        Long min,
        Long avg,
        Long max
) implements EmbeddableDTO {
    public PreferenceAmountDTO {
        if (min == null || min < 0) {
            throw new IllegalArgumentException("PreferenceAmountDTO parameter 'min' cannot be null or negative");
        }
        if (avg == null || avg < 0) {
            throw new IllegalArgumentException("PreferenceAmountDTO parameter 'avg' cannot be null or negative");
        }
        if (max == null || max < 0) {
            throw new IllegalArgumentException("PreferenceAmountDTO parameter 'max' cannot be null or negative");
        }
        if (min > avg) {
            throw new IllegalArgumentException("PreferenceAmountDTO parameter 'min' cannot be greater than the avg amount");
        }
        if (min > max) {
            throw new IllegalArgumentException("PreferenceAmountDTO parameter 'min' cannot be greater than max");
        }
        if (avg > max) {
            throw new IllegalArgumentException("PreferenceAmountDTO parameter 'avg' cannot be greater than max");
        }
    }
}