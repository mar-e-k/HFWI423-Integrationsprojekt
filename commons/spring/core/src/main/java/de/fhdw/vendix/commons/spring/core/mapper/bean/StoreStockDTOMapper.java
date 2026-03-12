package de.fhdw.vendix.commons.spring.core.mapper.bean;

import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.DTOMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = DTOMapperConfig.class)
public interface StoreStockDTOMapper extends DTOMapper<StoreStockDTO, StoreStockRequestDTO, StoreStockResponseDTO> {

    @Override
    StoreStockDTO toDomainDTO(StoreStockRequestDTO dto);

    @Override
    StoreStockResponseDTO toResponseDTO(StoreStockDTO storeStockDTO);
}