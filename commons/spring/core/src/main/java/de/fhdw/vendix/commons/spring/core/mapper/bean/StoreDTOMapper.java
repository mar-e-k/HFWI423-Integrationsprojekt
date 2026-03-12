package de.fhdw.vendix.commons.spring.core.mapper.bean;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreRequestDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreResponseDTO;
import de.fhdw.vendix.commons.spring.core.mapper.config.DTOMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = DTOMapperConfig.class)
public interface StoreDTOMapper extends DTOMapper<StoreDTO, StoreRequestDTO, StoreResponseDTO> {

    @Override
    StoreDTO toDomainDTO(StoreRequestDTO dto);

    @Override
    StoreResponseDTO toResponseDTO(StoreDTO storeDTO);
}