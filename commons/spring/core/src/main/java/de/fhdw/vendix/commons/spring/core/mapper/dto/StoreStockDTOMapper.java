package de.fhdw.vendix.commons.spring.core.mapper.dto;

import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                StoreDTOMapper.class,
                ArticleDTOMapper.class
        }
)
public interface StoreStockDTOMapper extends DTOMapper<StoreStockDTO, StoreStockRequestDTO, StoreStockResponseDTO> {

    @Override
    StoreStockDTO toDomainDTO(StoreStockRequestDTO dto);

    @Override
    StoreStockResponseDTO toResponseDTO(StoreStockDTO storeStockDTO);

    @Override
    List<StoreStockDTO> toDomainDTOs(Iterable<StoreStockRequestDTO> requestDTOs);

    @Override
    List<StoreStockResponseDTO> toResponseDTOs(Iterable<StoreStockDTO> domainDTOs);
}