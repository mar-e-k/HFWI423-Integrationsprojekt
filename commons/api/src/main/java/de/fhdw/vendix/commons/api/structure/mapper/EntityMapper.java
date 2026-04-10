package de.fhdw.vendix.commons.api.structure.mapper;

import de.fhdw.vendix.commons.api.structure.dto.DTO;

import java.util.List;

public interface EntityMapper<E, D extends DTO> extends Mapper {

    D toDTO(E entity);

    E toEntity(D dto);

    List<D> toDTOs(Iterable<E> entities);

    List<E> toEntities(Iterable<D> dtos);
}