package de.fhdw.vendix.commons.api.domain.store.dto;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import org.jspecify.annotations.Nullable;

// Should we include phone-number and or email?
public record StoreDTO(
        @Nullable Long id,
        String country, // should we have an enum for this?
        String city,
        String street,
        String streetNumber // this might need more validation with a regex
) implements DomainDTO<Long> {
    public StoreDTO {
        if (id != null && id < 0) {
            throw new IllegalArgumentException("StoreDTO parameter 'id' cannot be negative");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("StoreDTO parameter 'country' must not be null or blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("StoreDTO parameter 'city' must not be null or blank");
        }
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("StoreDTO parameter 'street' must not be null or blank");
        }
        if (streetNumber == null || streetNumber.isBlank()) {
            throw new IllegalArgumentException("StoreDTO parameter 'streetNumber' must not be null or blank and contain a number");
        }
    }

    @Override
    public @Nullable Long getIdentifiable() {
        return id;
    }
}