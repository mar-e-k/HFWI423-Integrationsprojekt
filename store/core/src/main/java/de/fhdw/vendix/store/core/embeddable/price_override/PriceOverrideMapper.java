package de.fhdw.vendix.store.core.embeddable.price_override;

import de.fhdw.vendix.commons.api.embeddable.PriceOverrideDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface PriceOverrideMapper extends EntityMapper<PriceOverride, PriceOverrideDTO> {

    @Override
    PriceOverrideDTO toDTO(PriceOverride entity);

    @Override
    PriceOverride toEntity(PriceOverrideDTO priceOverrideDTO);

    @Override
    List<PriceOverrideDTO> toDTOs(Iterable<PriceOverride> entities);

    @Override
    List<PriceOverride> toEntities(Iterable<PriceOverrideDTO> priceOverrideDTOS);
}