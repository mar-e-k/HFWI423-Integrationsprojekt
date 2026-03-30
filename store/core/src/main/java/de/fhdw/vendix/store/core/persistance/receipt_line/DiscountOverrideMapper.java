package de.fhdw.vendix.store.core.persistance.receipt_line;

import de.fhdw.vendix.commons.api.domain.receipt_line.DiscountOverrideDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface DiscountOverrideMapper extends EntityMapper<DiscountOverride, DiscountOverrideDTO> {

    @Override
    DiscountOverrideDTO toDTO(DiscountOverride entity);

    @Override
    DiscountOverride toEntity(DiscountOverrideDTO discountOverrideDTO);

    @Override
    List<DiscountOverrideDTO> toDTOs(Iterable<DiscountOverride> entities);

    @Override
    List<DiscountOverride> toEntities(Iterable<DiscountOverrideDTO> discountOverrideDTOS);
}