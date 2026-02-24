package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.structure.mapper.GenericDTOMapper;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreRequestDTO;
import de.fhdw.vendix.commons.api.domain.store.dto.StoreResponseDTO;
import org.mapstruct.Mapper;

@Mapper
public interface StoreDTOMapper extends GenericDTOMapper<StoreDTO, StoreRequestDTO, StoreResponseDTO> {}