package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.domain.store_system.dto.StoreSystemDTO;
import de.fhdw.vendix.commons.api.domain.store_system.dto.StoreSystemRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_system.dto.StoreSystemResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StoreSystemDTOMapper extends DTOMapper<StoreSystemDTO, StoreSystemRequestDTO, StoreSystemResponseDTO> {

    StoreSystemDTOMapper INSTANCE = Mappers.getMapper(StoreSystemDTOMapper.class);

    @Override
    StoreSystemRequestDTO toRequestDTO(StoreSystemDTO storeSystemDTO);

    @Override
    StoreSystemResponseDTO toResponseDTO(StoreSystemDTO storeSystemDTO);
}