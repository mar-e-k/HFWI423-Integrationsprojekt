package de.fhdw.vendix.orchestrator.core.domain.store;

import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreRequestDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreResponseDTO;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = SpringMapperConfig.class)
public interface StoreDTOMapper extends DTOMapper<StoreDTO, StoreRequestDTO, StoreResponseDTO> {

    @Override
    StoreDTO toDomainDTO(StoreRequestDTO dto);

    @Override
    StoreResponseDTO toResponseDTO(StoreDTO storeDTO);

    @Override
    Set<StoreDTO> toDomainDTOs(Iterable<StoreRequestDTO> requestDTOs);

    @Override
    Set<StoreResponseDTO> toResponseDTOs(Iterable<StoreDTO> domainDTOs);
}