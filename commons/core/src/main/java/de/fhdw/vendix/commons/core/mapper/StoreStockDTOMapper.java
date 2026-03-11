package de.fhdw.vendix.commons.core.mapper;

import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StoreStockDTOMapper extends DTOMapper<StoreStockDTO, StoreStockRequestDTO, StoreStockResponseDTO> {

    StoreStockDTOMapper INSTANCE = Mappers.getMapper(StoreStockDTOMapper.class);

    @Override
    StoreStockDTO toDTO(StoreStockRequestDTO dto);

    @Override
    StoreStockResponseDTO toResponseDTO(StoreStockDTO storeStockDTO);
}