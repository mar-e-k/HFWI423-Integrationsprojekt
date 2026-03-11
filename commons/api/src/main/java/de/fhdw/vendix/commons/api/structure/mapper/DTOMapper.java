package de.fhdw.vendix.commons.api.structure.mapper;

import de.fhdw.vendix.commons.api.structure.dto.DomainDTO;
import de.fhdw.vendix.commons.api.structure.dto.RequestDTO;
import de.fhdw.vendix.commons.api.structure.dto.ResponseDTO;

public interface DTOMapper<DTO extends Record & DomainDTO, REQ extends Record & RequestDTO, RES extends Record & ResponseDTO> extends Mapper {

    DTO toDTO(REQ dto);

    RES toResponseDTO(DTO dto);
}