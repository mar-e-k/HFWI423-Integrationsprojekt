package de.fhdw.vendix.commons.api.structure.mapper;

import java.util.List;

public interface EntityMapper<ENT, DTO> extends Mapper {

    DTO toDTO(ENT entity);

    ENT toEntity(DTO dto);

    List<DTO> toDTOs(Iterable<ENT> entities);

    List<ENT> toEntities(Iterable<DTO> dtos);
}