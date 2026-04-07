package de.fhdw.vendix.store.core.embeddable.preference_amount;

import de.fhdw.vendix.commons.api.embeddable.PreferenceAmountDTO;
import de.fhdw.vendix.commons.api.structure.mapper.EntityMapper;
import de.fhdw.vendix.commons.spring.data.mapper.SpringMapperConfig;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(config = SpringMapperConfig.class)
public interface PreferenceAmountMapper extends EntityMapper<PreferenceAmount, PreferenceAmountDTO> {

    @Override
    PreferenceAmountDTO toDTO(PreferenceAmount entity);

    @Override
    PreferenceAmount toEntity(PreferenceAmountDTO preferenceAmountDTO);

    @Override
    Set<PreferenceAmountDTO> toDTOs(Iterable<PreferenceAmount> entities);

    @Override
    Set<PreferenceAmount> toEntities(Iterable<PreferenceAmountDTO> preferenceAmountDTOS);
}