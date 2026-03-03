package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.domain.store_system.dto.StoreSystemDTO;
import de.fhdw.vendix.commons.api.domain.store_system.dto.StoreSystemRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_system.dto.StoreSystemResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SystemDTOMapper extends DTOMapper<StoreSystemDTO, StoreSystemRequestDTO, StoreSystemResponseDTO> {

    SystemDTOMapper INSTANCE = Mappers.getMapper(SystemDTOMapper.class);

    @Override
    StoreSystemRequestDTO toRequestDTO(StoreSystemDTO storeSystemDTO);

    @Override
    StoreSystemResponseDTO toResponseDTO(StoreSystemDTO storeSystemDTO);
}