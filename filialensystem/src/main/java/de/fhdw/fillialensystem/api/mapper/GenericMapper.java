package de.fhdw.fillialensystem.api.mapper;

import de.fhdw.commons.api.dto.GenericDTO;
import de.fhdw.commons.persistence.entity.GenericEntity;

public interface GenericMapper<E extends GenericEntity<?>, DTO extends GenericDTO<?>> {
    E toEntity(DTO dto);
    DTO toDto(E entity);
}