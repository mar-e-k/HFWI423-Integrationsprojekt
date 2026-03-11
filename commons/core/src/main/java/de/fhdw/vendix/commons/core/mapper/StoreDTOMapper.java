package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreRequestDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StoreDTOMapper extends DTOMapper<StoreDTO, StoreRequestDTO, StoreResponseDTO> {

    StoreDTOMapper INSTANCE = Mappers.getMapper(StoreDTOMapper.class);

    @Override
    StoreDTO toDTO(StoreRequestDTO dto);

    @Override
    StoreResponseDTO toResponseDTO(StoreDTO storeDTO);
}