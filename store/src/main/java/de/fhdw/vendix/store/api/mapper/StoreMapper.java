package de.fhdw.vendix.store.api.mapper;

import de.fhdw.vendix.commons.core.api.dto.StoreDTO;
import de.fhdw.vendix.commons.core.api.mapper.GenericMapper;
import de.fhdw.vendix.store.persistence.entity.Store;
import org.springframework.stereotype.Component;

@Component
public class StoreMapper implements GenericMapper<Store, StoreDTO> {

    public StoreMapper() {
        super();
    }

    @Override
    public Store toEntity(StoreDTO dto) {
        Store store = new Store();
        store.setId(dto.getId());
        store.setCountry(dto.getCountry());
        store.setCity(dto.getCity());
        store.setStreet(dto.getStreet());
        store.setStreetNumber(dto.getStreetNumber());
        return store;
    }

    @Override
    public StoreDTO toDto(Store store) {
        StoreDTO dto = new StoreDTO();
        dto.setId(store.getId());
        dto.setCountry(store.getCountry());
        dto.setCity(store.getCity());
        dto.setStreet(store.getStreet());
        dto.setStreetNumber(store.getStreetNumber());
        return dto;
    }
}
