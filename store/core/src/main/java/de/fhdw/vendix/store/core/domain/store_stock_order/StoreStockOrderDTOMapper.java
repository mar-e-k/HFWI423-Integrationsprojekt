package de.fhdw.vendix.store.core.domain.store_stock_order;

import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock_order.StoreStockOrderResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.data.persistance.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface StoreStockOrderDTOMapper extends DTOMapper<StoreStockOrderDTO, StoreStockOrderRequestDTO, StoreStockOrderResponseDTO> {

    @Override
    StoreStockOrderDTO toDomainDTO(StoreStockOrderRequestDTO requestDTO);

    @Override
    StoreStockOrderResponseDTO toResponseDTO(StoreStockOrderDTO domainDTO);

    @Override
    List<StoreStockOrderDTO> toDomainDTOs(Iterable<StoreStockOrderRequestDTO> requestDTOs);

    @Override
    List<StoreStockOrderResponseDTO> toResponseDTOs(Iterable<StoreStockOrderDTO> domainDTOs);
}