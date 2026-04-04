package de.fhdw.vendix.store.core.embeddable.preference_amount;

import de.fhdw.vendix.commons.api.embeddable.PreferenceAmountDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.core.mapper.config.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringMapperConfig.class)
public interface PreferenceAmountMapper extends EntityMapper<PreferenceAmount, PreferenceAmountDTO> {

    @Override
    PreferenceAmountDTO toDTO(PreferenceAmount entity);

    @Override
    PreferenceAmount toEntity(PreferenceAmountDTO preferenceAmountDTO);

    @Override
    List<PreferenceAmountDTO> toDTOs(Iterable<PreferenceAmount> entities);

    @Override
    List<PreferenceAmount> toEntities(Iterable<PreferenceAmountDTO> preferenceAmountDTOS);
}