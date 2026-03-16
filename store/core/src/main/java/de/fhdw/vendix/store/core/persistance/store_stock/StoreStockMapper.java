package de.fhdw.vendix.store.core.persistance.store_stock;

import de.fhdw.vendix.commons.api.domain.store_stock.dto.StoreStockDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import de.fhdw.vendix.store.core.persistance.article.ArticleMapper;
import de.fhdw.vendix.store.core.persistance.store.StoreMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                StoreMapper.class,
                ArticleMapper.class,
        }
)
public interface StoreStockMapper extends EntityMapper<Long, StoreStock, StoreStockDTO> {

    @Override
    StoreStockDTO toDTO(StoreStock entity);

    @Override
    StoreStock toEntity(StoreStockDTO storeStockDTO);

    @Override
    List<StoreStockDTO> toDTOs(Iterable<StoreStock> entities);

    @Override
    List<StoreStock> toEntities(Iterable<StoreStockDTO> storeStockDTOS);
}