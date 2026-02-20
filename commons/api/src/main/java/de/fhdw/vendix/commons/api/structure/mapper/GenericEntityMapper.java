package de.fhdw.vendix.commons.api.structure.mapper;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;

public interface GenericEntityMapper<
        ENT,
        DTO extends Record & DomainDTO
        > extends Mapper {
    DTO toDTO(ENT entity);

    ENT toEntity(DTO dto);
}