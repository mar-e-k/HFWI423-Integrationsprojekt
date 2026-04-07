package de.fhdw.vendix.commons.api.structure.mapper;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;
import de.fhdw.vendix.commons.api.structure.dto.ResponseDTO;

import java.util.Set;

public interface DTOMapper<
        DOM extends DomainDTO,
        REQ extends RequestDTO,
        RES extends ResponseDTO
        > extends Mapper {

    DOM toDomainDTO(REQ requestDTO);

    RES toResponseDTO(DOM domainDTO);

    Set<DOM> toDomainDTOs(Iterable<REQ> requestDTOs);

    Set<RES> toResponseDTOs(Iterable<DOM> domainDTOs);
}