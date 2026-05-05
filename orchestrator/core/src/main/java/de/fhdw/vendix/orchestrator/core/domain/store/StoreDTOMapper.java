package de.fhdw.vendix.orchestrator.core.domain.store;

import de.fhdw.vendix.commons.api.domain.store.StoreDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreRequestDTO;
import de.fhdw.vendix.commons.api.domain.store.StoreResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.data.persistance.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface StoreDTOMapper extends DTOMapper<StoreDTO, StoreRequestDTO, StoreResponseDTO> {

    @Override
    StoreDTO toDomainDTO(StoreRequestDTO dto);

    @Override
    StoreResponseDTO toResponseDTO(StoreDTO storeDTO);

    @Override
    List<StoreDTO> toDomainDTOs(Iterable<StoreRequestDTO> requestDTOs);

    @Override
    List<StoreResponseDTO> toResponseDTOs(Iterable<StoreDTO> domainDTOs);
}