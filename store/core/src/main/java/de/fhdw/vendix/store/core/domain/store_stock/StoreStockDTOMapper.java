package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.api.domain.store_stock.StoreStockDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.StoreStockRequestDTO;
import de.fhdw.vendix.commons.api.domain.store_stock.StoreStockResponseDTO;
import de.fhdw.vendix.commons.api.structure.mapper.DTOMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import de.fhdw.vendix.store.core.domain.article.ArticleDTOMapper;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                ArticleDTOMapper.class
        }
)
public interface StoreStockDTOMapper extends DTOMapper<StoreStockDTO, StoreStockRequestDTO, StoreStockResponseDTO> {

    @Override
    StoreStockDTO toDomainDTO(StoreStockRequestDTO dto);

    @Override
    StoreStockResponseDTO toResponseDTO(StoreStockDTO storeStockDTO);

    @Override
    Set<StoreStockDTO> toDomainDTOs(Iterable<StoreStockRequestDTO> requestDTOs);

    @Override
    Set<StoreStockResponseDTO> toResponseDTOs(Iterable<StoreStockDTO> domainDTOs);
}