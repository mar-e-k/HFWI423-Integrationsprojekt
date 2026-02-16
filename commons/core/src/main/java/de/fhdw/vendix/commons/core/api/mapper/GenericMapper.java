package de.fhdw.vendix.commons.core.api.mapper;

import de.fhdw.vendix.commons.core.api.dto.GenericDTO;
import de.fhdw.vendix.commons.core.persistence.entity.GenericEntity;

public interface GenericMapper<E extends GenericEntity<?>, DTO extends GenericDTO<?>> {
    E toEntity(DTO dto);
    DTO toDto(E entity);
}