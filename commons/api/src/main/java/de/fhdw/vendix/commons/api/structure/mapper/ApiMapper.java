package de.fhdw.vendix.commons.api.structure.mapper;

import java.util.List;

public interface ApiMapper<DTO, API> extends Mapper {

    API toAPI(DTO dto);

    DTO toDTO(API api);

    List<API> toAPIs(Iterable<DTO> dtos);

    List<DTO> toDTOs(Iterable<API> apis);
}