package de.fhdw.vendix.commons.api.structure.mapper;

import de.fhdw.vendix.commons.api.structure.dto.DTO;

import java.util.Set;

public interface EntityMapper<E, D extends DTO> extends Mapper {

    D toDTO(E entity);

    E toEntity(D dto);

    Set<D> toDTOs(Iterable<E> entities);

    Set<E> toEntities(Iterable<D> dtos);
}