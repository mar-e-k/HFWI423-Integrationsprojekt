package de.fhdw.vendix.store.core.persistance.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.PriceOverrideDTO;
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