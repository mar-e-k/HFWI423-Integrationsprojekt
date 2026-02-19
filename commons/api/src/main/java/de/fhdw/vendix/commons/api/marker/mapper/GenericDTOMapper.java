package de.fhdw.vendix.commons.api.marker.mapper;

import de.fhdw.vendix.commons.api.marker.dto.DomainDTO;
import de.fhdw.vendix.commons.api.marker.dto.RequestDTO;
import de.fhdw.vendix.commons.api.marker.dto.ResponseDTO;

public interface GenericDTOMapper<
        DTO extends Record & DomainDTO,
        REQ extends Record & RequestDTO,
        RES extends Record & ResponseDTO
        > {
    REQ toRequestDTO(DTO dto);

    RES toResponseDTO(DTO dto);
}