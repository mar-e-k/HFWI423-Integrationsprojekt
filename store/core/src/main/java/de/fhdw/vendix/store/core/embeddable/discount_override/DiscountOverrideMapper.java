package de.fhdw.vendix.store.core.embeddable.discount_override;

import de.fhdw.vendix.commons.api.embeddable.DiscountOverrideDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = SpringMapperConfig.class)
public interface DiscountOverrideMapper extends EntityMapper<DiscountOverride, DiscountOverrideDTO> {

    @Override
    DiscountOverrideDTO toDTO(DiscountOverride entity);

    @Override
    DiscountOverride toEntity(DiscountOverrideDTO discountOverrideDTO);

    @Override
    Set<DiscountOverrideDTO> toDTOs(Iterable<DiscountOverride> entities);

    @Override
    Set<DiscountOverride> toEntities(Iterable<DiscountOverrideDTO> discountOverrideDTOS);
}