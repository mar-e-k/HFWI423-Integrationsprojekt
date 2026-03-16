package de.fhdw.vendix.commons.api.structure.mapper;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import de.fhdw.vendix.commons.api.structure.entity.Identifiable;

import java.util.List;

public interface EntityMapper<
        ID,
        ENT extends Identifiable<ID>,
        DTO extends DomainDTO<ID>> extends Mapper {

    DTO toDTO(ENT entity);

    ENT toEntity(DTO dto);

    List<DTO> toDTOs(Iterable<ENT> entities);

    List<ENT> toEntities(Iterable<DTO> dtos);
}