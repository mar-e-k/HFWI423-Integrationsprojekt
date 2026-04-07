package de.fhdw.vendix.store.core.domain.store_stock;

import de.fhdw.vendix.commons.api.domain.store_stock.StoreStockDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import de.fhdw.vendix.store.core.domain.article.ArticleMapper;
import de.fhdw.vendix.store.core.domain.store.StoreMapper;
import de.fhdw.vendix.store.core.embeddable.preference_amount.PreferenceAmountMapper;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(
        config = SpringMapperConfig.class,
        uses = {
                StoreMapper.class,
                ArticleMapper.class,
                PreferenceAmountMapper.class,
        }
)
public interface StoreStockMapper extends EntityMapper<StoreStock, StoreStockDTO> {

    @Override
    StoreStockDTO toDTO(StoreStock entity);

    @Override
    StoreStock toEntity(StoreStockDTO storeStockDTO);

    @Override
    Set<StoreStockDTO> toDTOs(Iterable<StoreStock> entities);

    @Override
    Set<StoreStock> toEntities(Iterable<StoreStockDTO> storeStockDTOS);
}